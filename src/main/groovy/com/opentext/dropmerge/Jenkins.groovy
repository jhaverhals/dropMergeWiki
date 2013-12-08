package com.opentext.dropmerge

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

    public static def getTestDiffsPerPackage(List<JenkinsJob> beforeJob, List<JenkinsJob> afterJob) {
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

    public static Map<String, Integer> getTestDiffsPerSuite(JenkinsJob beforeJob, JenkinsJob afterJob) {
        return getTestDiffsPerSuite([beforeJob], [afterJob])
    }

    public static Map<String, Integer> getTestDiffsPerSuite(List<JenkinsJob> beforeJobs, List<JenkinsJob> afterJobs) {

        //Metaclass extension
        ArrayList.metaClass.collectMap = { Closure<List<Object>> callback ->
            def map = [:]
            delegate.each {
                List<Object> r = callback.call(it)
                if (r && r.size() > 0)
                    map[r[0]] = r[1]
            }
            return map
        }

        def casesPerSuite = {
            if (it.suites) {
                return it.suites.collectMap {
                    int conSkippedCasesCount = it.cases.findAll { c -> c.status != 'SKIPPED' }.size() as int
                    if (conSkippedCasesCount > 0)
                        return [it.name, conSkippedCasesCount]
                }
            }

            def map = [:]
            it.childReports.each {
                map << it.result.suites.collectMap {
                    int conSkippedCasesCount = it.cases.findAll { c -> c.status != 'SKIPPED' }.size() as int
                    if (conSkippedCasesCount > 0)
                        return [it.name, conSkippedCasesCount]
                }
            }
            return map
        }

        def suitesBefore = join(beforeJobs.collect { casesPerSuite(it.testReport) })
        def suitesAfter = join(afterJobs.collect { casesPerSuite(it.testReport) })

        Map<String, Integer> diffsPerSuite = new TreeMap<String, Integer>()

        for (Map.Entry<String, Integer> kvp : suitesAfter) {
            if (!suitesBefore.containsKey(kvp.key))
                diffsPerSuite.put(kvp.key, kvp.value)
            else if (suitesBefore[kvp.key] != kvp.value)
                diffsPerSuite.put(kvp.key, kvp.value - suitesBefore[kvp.key])
        }
        for (Map.Entry<String, Integer> kvp : suitesBefore) {
            if (!suitesAfter.containsKey(kvp.key))
                diffsPerSuite.put(kvp.key, -kvp.value)
        }

        return diffsPerSuite
    }

    public static Map<String, Integer> join(List<Map<String, Integer>> list) {
        if (list.size() == 1)
            return list.first()

        Map<String, Integer> result = list.first()
        list.listIterator(1).each {
            result = join(result, it)
        }
        return result
    }

    static Map<String, Integer> join(Map<String, Integer> a, Map<String, Integer> b) {
        Map<String, Integer> result = ([:] << a)
        b.each { kvp ->
            if (result.containsKey(kvp.key))
                result[kvp.key] += kvp.value
            else
                result[kvp.key] = kvp.value
        }
        return result
    }

    static Map<String, Integer> casesPerSuite(Object jsonRoot, List<String> priorities = ['NORMAL', 'HIGH']) {
        ArrayList.metaClass.collectMap = { Closure<List<Object>> callback ->
            def map = [:]
            delegate.each {
                List<Object> r = callback.call(it)
                if (r && r.size() > 0) {
                    if (map.containsKey(r[0]))
                        map[r[0]] += r[1]
                    else
                        map[r[0]] = r[1]
                }
            }
            return map
        }

        final List<String> prios = priorities
        def casesPerSuite = {
            return it.warnings.collectMap {
                if (prios.contains(it.priority))
                    return [it.fileName, 1]
            }
        }
        return casesPerSuite(jsonRoot)
    }

    static Map<String, String> correlateKeys(Set<String> a, Set<String> b) {
        Map<String, String> a2b = new TreeMap<String, String>()
        a.each { String before ->
            int matchCount = -1
            a2b[before] = null
            b.each { String it ->
                int m = match(before, it)
                if (m > matchCount) {
                    matchCount = m
                    a2b[before] = it
                }
            }
        }
        return a2b
    }

    public static Map<String, Integer> getPMDDiffsPerSuite(JenkinsJob beforeJob, JenkinsJob afterJob, List<String> priorities = ['NORMAL', 'HIGH']) {
        Map<String, Integer> suitesBefore = casesPerSuite(beforeJob.PMDReport, priorities)
        Map<String, Integer> suitesAfter = casesPerSuite(afterJob.PMDReport, priorities)

        Map<String, String> beforeToAfter = correlateKeys(suitesBefore.keySet(), suitesAfter.keySet());
        Map<String, String> afterToBefore = correlateKeys(suitesAfter.keySet(), suitesBefore.keySet());

        Map<String, Integer> diffsPerSuite = new TreeMap<String, Integer>()

        for (Map.Entry<String, Integer> kvp : suitesAfter) {
            String beforeKey = afterToBefore[kvp.key]
            if (!suitesBefore.containsKey(beforeKey))
                diffsPerSuite.put(kvp.key, kvp.value)
            else {
                if (suitesBefore[beforeKey] != kvp.value) {
                    String name = kvp.key.substring(kvp.key.length() - match(kvp.key, beforeKey) + 1)
                    diffsPerSuite.put(kvp.key, kvp.value - suitesBefore[beforeKey])
                }
            }
        }
        for (Map.Entry<String, Integer> kvp : suitesBefore) {
            String afterKey = beforeToAfter[kvp.key]
            if (!suitesAfter.containsKey(afterKey))
                diffsPerSuite.put(kvp.key, -kvp.value)
        }

        return diffsPerSuite
    }

    static int match(String a, String b) {
        if (fileName(a) != fileName(b))
            return -1
        if (fileNamePlus(a) != fileNamePlus(b))
            return -1

        int i = 0
        a.reverse().takeWhile { it == b.charAt(b.length() - ++i) }
        return i - 1
    }

    static String fileName(String path) {
        path.substring(path.lastIndexOf('/'))
    }

    static String fileNamePlus(String path) {
        int i = 2;
        return path.reverse().takeWhile {
            it == '/' && --i == 0
        }.reverse()
    }
}
