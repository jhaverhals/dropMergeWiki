public class Jenkins {

    private final String rootUrl;

    public Jenkins(String rootUrl) {
        this.rootUrl = rootUrl
    }

    public JenkinsJob withJob(String name) {
        return new JenkinsJob(this, name);
    }

    String getRootUrl() {
        return rootUrl
    }

    public static def getTestDiffsPerPackage(JenkinsJob beforeJob, JenkinsJob afterJob) {
        getTestDiffsPerPackage(getTestDiffsPerSuite(beforeJob, afterJob))
    }

    public static def getTestDiffsPerPackage(Map<String, Integer> diffsPerSuite) {
        def diffsPerPackage = new TreeMap<String, Integer>()
        for (def kvp : diffsPerSuite) {
            String packageName = kvp.key.substring(0, kvp.key.lastIndexOf('.'))
            if (diffsPerPackage.containsKey(packageName))
                diffsPerPackage[packageName] += kvp.value
            else
                diffsPerPackage[packageName] = kvp.value
        }
    }

    private static Map<String, Integer> getTestDiffsPerSuite(JenkinsJob beforeJob, JenkinsJob afterJob) {

        //Metaclass extension
        ArrayList.metaClass.collectMap = { Closure<List<Object>> callback ->
            def map = [:]
            delegate.each {
                List<Object> r = callback.call(it)
                map[r[0]] = r[1]
            }
            return map
        }

        def casesPerSuite = { return it.suites.collectMap { [it.name, it.cases.size() as int] } }

        def suitesBefore = casesPerSuite(beforeJob.testReport)
        def suitesAfter = casesPerSuite(afterJob.testReport)

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
