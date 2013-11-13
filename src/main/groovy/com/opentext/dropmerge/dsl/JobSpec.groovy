package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.JenkinsJob;
class JobSpec {
        Jenkins jenkinsInstance
        String jobName
        String description

        def on(Jenkins instance) { this.jenkinsInstance = instance; return this }

        JobSpec job(String jobName) { this.jobName = jobName; return this }

        def description(String description) { this.description = description; return this }

        public JenkinsJob getJenkinsJob() {
            if (jenkinsInstance == null)
                throw new IllegalStateException('jenkinsInstance should not be null')
            if (jobName == null)
                throw new IllegalStateException('jobName should not be null')

            return jenkinsInstance.withJob(jobName)
        }

        String getDescription() {
            return description
        }
    }