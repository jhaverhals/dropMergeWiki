import groovy.json.JsonSlurper;


public class Jenkins {
    private static int invocationCount = 0
    public static final String LAST_COMPLETED_BUILD = 'lastCompletedBuild'
    public static final String LAST_SUCCESSFUL_BUILD = 'lastSuccessfulBuild'
    private static final Map<String, Object> jsonCache = new HashMap<>()

    static {
        addShutdownHook {
            println 'Called for information:    '+Jenkins.invocationCount    +' times'
            println 'Final cache size:          '+jsonCache.size()
            jsonCache.keySet().each {println it}
        }
    }

    static def getLastBuildResult(String job) {
        jsonForJob(job, LAST_COMPLETED_BUILD, null, 'result')['result'].toString()
    }

    static def getPropertyOfJobWithinReports(String job, String report, String prop) {
        jsonForJob(job, report, prop)[prop].toString()
    }

    static def getPropertyOfJobWithinReports(String job, String report, JenkinsJsonField prop) {
        jsonForJob(job, report, prop)[prop.jsonField].toString()
    }

    static def getTestFigure(String job, TestCount testCount) {
        getPropertyOfJobWithinReports(job, 'testReport', testCount)
    }

    static def getTestFigure(String job, TestCount testCount, TestCount... minus) {
        int total = getTestFigure(job, testCount) as int
        minus.each { total -= getTestFigure(job, it) as int }
        return total
    }

    static def getPMDFigure(String job, WarningLevel level) {
        getPropertyOfJobWithinReports(job, 'pmdResult', level)
    }

    static def getCompilerWarningFigure(String job) {
        getPropertyOfJobWithinReports(job, 'warnings2Result', 'numberOfWarnings')
    }

    static def getMBFigure(String job, WarningLevel level) {
        getPropertyOfJobWithinReports(job, 'muvipluginResult', level)
    }

    static def jsonForJob(String job, String build, String subPage, String jsonPath) {
        invocationCount++
        final url = "http://buildmaster-nl.vanenburg.com/jenkins/job/" + job + "/" + build + "/" + (subPage ? subPage + '/' : '') + "api/json" + (jsonPath ? '?tree=' + jsonPath : '')
        jsonCache[url] ?: (jsonCache[url] = new JsonSlurper().parseText(new URL(url).text))
    }

    static def jsonForJob(String job, String subPage, String jsonPath) {
        jsonForJob(job, LAST_SUCCESSFUL_BUILD, subPage, jsonPath)
    }

    static def jsonForJob(String job, String subPage, JenkinsJsonField jsonPath) {
        jsonForJob(job, subPage, jsonPath.allValues())
    }

    static def getTestDiffsPerPackage(String beforeJob, String afterJob) {
        getTestDiffsPerPackage(getTestDiffsPerSuite(beforeJob, afterJob))
    }

    static def getTestDiffsPerPackage(Map<String, Integer> diffsPerSuite) {
        def diffsPerPackage = new TreeMap<String, Integer>()
        for (def kvp : diffsPerSuite) {
            String packageName = kvp.key.substring(0, kvp.key.lastIndexOf('.'))
            if (diffsPerPackage.containsKey(packageName))
                diffsPerPackage[packageName] += kvp.value
            else
                diffsPerPackage[packageName] = kvp.value
        }
    }

    static Map<String, Integer> getTestDiffsPerSuite(String beforeJob, String afterJob) {

        //Metaclass extension
        ArrayList.metaClass.collectMap = { Closure callback ->
            def map = [:]
            delegate.each {
                def r = callback.call(it)
                map[r[0]] = r[1]
            }
            return map
        }

        def casesPerSuite = { return it.suites.collectMap { [it.name, it.cases.size() as int] } }

        def beforeJson = jsonForJob(beforeJob, LAST_SUCCESSFUL_BUILD, 'testReport', null)
        def afterJson = jsonForJob(afterJob, LAST_SUCCESSFUL_BUILD, 'testReport', null)

        def suitesBefore = casesPerSuite(beforeJson)
        def suitesAfter = casesPerSuite(afterJson)

        def diffsPerSuite = new TreeMap<String, Integer>()

        for (def kvp : suitesAfter) {
            if (!suitesBefore.containsKey(kvp.key))
                diffsPerSuite.put(kvp.key, kvp.value)
            else if (suitesBefore[kvp.key] != kvp.value)
                diffsPerSuite.put(kvp.key, kvp.value - suitesBefore[kvp.key])
        }
        for (def kvp : suitesBefore) {
            if (!suitesAfter.containsKey(kvp.key))
                diffsPerSuite.put(kvp.key, -kvp.value)
        }

        return diffsPerSuite
    }


    public interface JenkinsJsonField {
        String name()

        String allValues()

        String getJsonField()
    }
}
