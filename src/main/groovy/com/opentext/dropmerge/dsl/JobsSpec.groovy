package com.opentext.dropmerge.dsl;

class JobsSpec {
    List<JobSpec> jobs = [];

    def withJob(@DelegatesTo(JobSpec) trunkJob) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with trunkJob

        jobs << jobSpec
    }

    List<JobSpec> getJobs() {
        return jobs
    }
}