import Jenkins
import groovy.json.JsonSlurper

class JenkinsJob {
    private static int invocationCount = 0
    private static final Map<String, Object> jsonCache = new HashMap<>()

    public static final String LAST_COMPLETED_BUILD = 'lastCompletedBuild'
    public static final String LAST_SUCCESSFUL_BUILD = 'lastSuccessfulBuild'

    private Jenkins onInstance;
    private String name

    JenkinsJob(Jenkins onInstance, String name) {
        this.onInstance = onInstance
        this.name = name
    }

    public String getLastBuildResult() {
        jsonForJob(LAST_COMPLETED_BUILD, null, 'result')['result'].toString()
    }

    private String getPropertyOfJobWithinReports(String report, String prop) {
        jsonForJob(report, prop)[prop].toString()
    }

    private String getPropertyOfJobWithinReports(String report, JenkinsJsonField prop) {
        jsonForJob(report, prop)[prop.jsonField].toString()
    }

    public String getTestFigure(TestCount testCount) {
        getPropertyOfJobWithinReports('testReport', testCount)
    }

    public String getTestFigureMultiConfig(TestCount testCount) {
        int total = 0
        jsonForJob(null, null, "activeConfigurations[name]")["activeConfigurations"].each {
            if(!it.name.contains("component=cap"))
                total += onInstance.withJob("$name/${it.name}").getPropertyOfJobWithinReports('testReport', testCount) as int
        }
        return "$total"
    }

    public int getTestFigure(TestCount testCount, TestCount... minus) {
        int total = getTestFigure(testCount) as int
        minus.each { total -= getTestFigure(it) as int }
        return total
    }

    public int getTestFigureMultiConfig(TestCount testCount, TestCount... minus) {
        int total = getTestFigureMultiConfig(testCount) as int
        minus.each { total -= getTestFigureMultiConfig(it) as int }
        return total
    }

    public def getTestReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'testReport', null)
    }

    public String getPMDFigure(WarningLevel level) {
        getPropertyOfJobWithinReports('pmdResult', level)
    }

    public String getCompilerWarningFigure() {
        getPropertyOfJobWithinReports('warnings2Result', 'numberOfWarnings')
    }

    public String getMBFigure(WarningLevel level) {
        getPropertyOfJobWithinReports('muvipluginResult', level)
    }

    private def jsonForJob(String build, String subPage, String jsonPath) {
        invocationCount++
        final url = getBuildUrl(build) + '/' + (subPage ? subPage + '/' : '') + 'api/json' + (jsonPath ? '?tree=' + jsonPath : '')
        jsonCache[url] ?: (jsonCache[url] = new JsonSlurper().parseText(new URL(url).text))
    }

    private def jsonForJob(String subPage, String jsonPath) {
        jsonForJob(LAST_SUCCESSFUL_BUILD, subPage, jsonPath)
    }

    private def jsonForJob(String subPage, JenkinsJsonField jsonPath) {
        jsonForJob(subPage, jsonPath.allValues())
    }

    public String getJobUrl() {
        onInstance.rootUrl + '/job/' + name
    }

    public String getBuildUrl(String build) {
        if(!build)
            jobUrl
        else
            jobUrl + '/' + build
    }
}
