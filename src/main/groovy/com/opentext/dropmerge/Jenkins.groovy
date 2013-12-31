package com.opentext.dropmerge

import groovy.transform.Memoized

public class Jenkins {

    private final String rootUrl;

    public Jenkins(String rootUrl) {
        this.rootUrl = rootUrl
    }

    @Memoized
    public JenkinsJob withJob(String name, Map<String, String> matrixAxes = null) {
        return new JenkinsJob(this, name, matrixAxes);
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

    static Map<String, Integer> casesPerSuite(Object jsonRoot) {
        if (jsonRoot.suites) {
            return countCasesBySuite(jsonRoot)
        }

        return jsonRoot.childReports.inject([:]) { map, childReport ->
            map << countCasesBySuite(childReport.result)
        }
    }

    private static Map<String, Integer> countCasesBySuite(Object jsonRoot) {
        return jsonRoot.suites
                .collectEntries { [(it.name): (it.cases.findAll { c -> c.status != 'SKIPPED' }.size() as int)] }
                .findAll { it.value > 0 }
    }

    static Map<String, Integer> violationsPerSuite(Object jsonRoot, final List<String> priorities = ['NORMAL', 'HIGH']) {
        return jsonRoot.warnings
                .findAll { priorities.contains(it.priority) }
                .groupBy { it.fileName }
                .collectEntries { [(it.key): it.value.size()] }
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
        return getDiffsPerSuite(beforeJob.PMDReport, afterJob.PMDReport, priorities)
    }
    public static Map<String, Integer> getMBVDiffsPerSuite(JenkinsJob beforeJob, JenkinsJob afterJob, List<String> priorities = ['NORMAL', 'HIGH']) {
        return getDiffsPerSuite(beforeJob.MBVReport, afterJob.MBVReport, priorities)
    }
    private static Map<String, Integer> getDiffsPerSuite(def beforeJobReport, def afterJobReport, List<String> priorities = ['NORMAL', 'HIGH']) {
        Map<String, Integer> suitesBefore = violationsPerSuite(beforeJobReport, priorities)
        Map<String, Integer> suitesAfter = violationsPerSuite(afterJobReport, priorities)

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
