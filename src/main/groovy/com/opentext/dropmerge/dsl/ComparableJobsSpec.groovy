package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.JenkinsJob
import groovy.transform.TypeChecked

@TypeChecked
class ComparableJobsSpec {
    JenkinsJob trunk, wip

    void trunk(@DelegatesTo(JobSpec) Closure trunkJob) {
        trunk = invoke(trunkJob)
    }

    void wip(@DelegatesTo(JobSpec) Closure wipJob) {
        wip = invoke(wipJob)
    }

    private static JenkinsJob invoke(@DelegatesTo(JobSpec) Closure jobClosure) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with jobClosure
        return jobSpec.jenkinsJob
    }

}