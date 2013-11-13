package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.JenkinsJob;
class ComparableJobsSpec {
    JenkinsJob trunk, wip

    def trunk(@DelegatesTo(JobSpec) trunkJob) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with trunkJob

        trunk = jobSpec.jenkinsJob
    }

    def wip(@DelegatesTo(JobSpec) wipJob) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with wipJob

        wip = jobSpec.jenkinsJob
    }

}