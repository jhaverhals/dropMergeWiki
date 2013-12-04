package com.opentext.dropmerge.dsl


class TestTypesSpec {
    private Map<String, List<JobSpec>> jobsByType = new TreeMap<>()
    private Map<String, Map<JobSpec, JobSpec>> comparableJobsByType = new TreeMap<>()
    private Map<String, Map<JobSpec, ComparingJobsSpec.DifferencesSpec>> justifications = new TreeMap<>()
    FreeTextSpec extraComment = new FreeTextSpec()

    def ofType(String typeName, @DelegatesTo(ComparingJobsSpec) jobsClosure) {
        ComparingJobsSpec jobsSpec = new ComparingJobsSpec()
        jobsSpec.with jobsClosure

        jobsByType[typeName] = jobsSpec.jobs
        comparableJobsByType[typeName] = jobsSpec.comparableJobSpecs
        justifications[typeName] = jobsSpec.justifications
    }

    Map<String, List<JobSpec>> getJobsByType() {
        return jobsByType
    }

    Map<String, Map<JobSpec, JobSpec>> getComparableJobsByType() {
        return comparableJobsByType
    }

    Map<String, Map<JobSpec, ComparingJobsSpec.DifferencesSpec>> getJustifications() {
        return justifications
    }

    def extraComment(@DelegatesTo(FreeTextSpec) freeTextClosure) {
        extraComment.with freeTextClosure
    }
}
