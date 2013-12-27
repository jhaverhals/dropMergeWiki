package com.opentext.dropmerge

import groovy.json.JsonSlurper

class JenkinsJob {
    private static int invocationCount = 0
    private static final Map<String, Object> jsonCache = new HashMap<>()

    public static final String LAST_COMPLETED_BUILD = 'lastCompletedBuild'
    public static final String LAST_SUCCESSFUL_BUILD = 'lastSuccessfulBuild'

    private Jenkins onInstance;
    private String name
    private Map<String, String> matrixAxes

    JenkinsJob(Jenkins onInstance, String name, Map<String, String> matrixAxes = null) {
        this.onInstance = onInstance
        this.name = name
        this.matrixAxes = matrixAxes

        if (matrixAxes) {
            List<String[]> x = jsonForJob(null, null, 'activeConfigurations[name]')['activeConfigurations'].collect { it.name.split(',') }
            def matches = x.findAll { String[] configuration ->
                matrixAxes.every { String axis, String value ->
                    configuration.contains("$axis=$value")
                }
            }
            if (matches.size() == 0)
                throw new IllegalArgumentException("No configuration matches $matrixAxes for job $this");
            if (matches.size() > 1)
                throw new IllegalArgumentException("Multiple configuration matches $matrixAxes for job $this");

            this.name += '/' + matches.first().join(',')
        }
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
        if (matrixSubJobs.isEmpty())
            return getPropertyOfJobWithinReports('testReport', testCount)
        else
            return getTestFigureMultiConfig(testCount)
    }

    public String getTestFigureMultiConfig(TestCount testCount) {
        int total = 0
        getMatrixSubJobs().each {
            total += it.getPropertyOfJobWithinReports('testReport', testCount) as int
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
        final String path = 'suites[name,cases[status]]'
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'testReport', "$path,childReports[result[$path]]")
    }

    public def getPMDReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'pmdResult', 'warnings[priority,fileName]')
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

    public def getMBVReport() {
        jsonForJob(LAST_SUCCESSFUL_BUILD, 'muvipluginResult', 'warnings[priority,fileName]')
    }

    private def jsonForJob(String build, String subPage, String jsonPath, Integer depth = null) {
        invocationCount++
        final url = getBuildUrl(build) + '/' + (subPage ? subPage + '/' : '') + 'api/json' + (jsonPath ? '?tree=' + jsonPath + (depth ? '&' : '') : (depth ? '?' : '')) + (depth ? "depth=$depth" : '')
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
        if (!build)
            jobUrl
        else
            jobUrl + '/' + build
    }

    public def getMatrixSubJobs() {
        def subJobs = []
        jsonForJob(null, null, "activeConfigurations[name]")["activeConfigurations"].each {
            subJobs.add onInstance.withJob("$name/${it.name}")
        }
        return subJobs
    }

    public String getColor() {
        return jsonForJob(null, null, 'color')['color'].toString()
    }

    @Override
    public java.lang.String toString() {
        String n
        if (matrixAxes) {
            n = "'" + name.substring(0, name.indexOf('/')) + "' with $matrixAxes"
        } else {
            n = "'$name'"
        }
        return n + ' on ' + new URL(onInstance.rootUrl).host.replaceFirst('.vanenburg.com$', '');
    }
}
