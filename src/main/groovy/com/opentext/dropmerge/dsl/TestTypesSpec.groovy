package com.opentext.dropmerge.dsl



class TestTypesSpec {
    private Map<String, List<JobSpec>> jobsByType = new LinkedHashMap<>()
    private Map<String, Map<List<JobSpec>, List<JobSpec>>> comparableJobsByType = new TreeMap<>()
    private Map<String, Map<JobSpec, ComparingJobsSpec.DifferencesSpec>> justifications = new TreeMap<>()
    FreeTextSpec extraComment = new FreeTextSpec()

    def ofType(String typeName, @DelegatesTo(ComparingJobsSpec) jobsClosure) {
        ComparingJobsSpec jobsSpec = new ComparingJobsSpec()
        jobsSpec.with jobsClosure

        jobsByType[typeName] = jobsSpec.jobs
        comparableJobsByType[typeName] = jobsSpec.comparableJobSpecs.collectEntries { key, value ->
					[jobsSpec.getJobSpecPlusLinkedJobSpecs(key), jobsSpec.getJobSpecPlusLinkedJobSpecs(value)]
				}
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
