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

        def suitesBefore = casesPerSuite(beforeJob.testReport)
        def suitesAfter = casesPerSuite(afterJob.testReport)

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

    public static Map<String, Integer> getPMDDiffsPerSuite(JenkinsJob beforeJob, JenkinsJob afterJob) {

        //Metaclass extension
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

        final List<String> prios = ['NORMAL', 'HIGH']
        def casesPerSuite = {
            return it.warnings.collectMap {
                if (prios.contains(it.priority))
                    return [it.fileName, 1]
            }
        }

        Map<String, Integer> suitesBefore = casesPerSuite(beforeJob.PMDReport)
        Map<String, Integer> suitesAfter = casesPerSuite(afterJob.PMDReport)

       /* new File('pmdAfter.txt').with { f ->
            write('')
            suitesAfter.each { f.append(it.key + '     ' + it.value + System.lineSeparator()) }

        }  */

        Map<String, String> beforeToAfter = new TreeMap<String, String>()
        Map<String, String> afterToBefore = new TreeMap<String, String>()
        suitesBefore.keySet().each { String before ->
            int matchCount = -1
            beforeToAfter[before] = null
            suitesAfter.keySet().each { String it ->
                int m = match(before, it)
                if (m > matchCount) {
                    matchCount = m
                    beforeToAfter[before] = it
                    afterToBefore[it] = before

                }
            }
        }

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
        } .reverse()
    }
}
