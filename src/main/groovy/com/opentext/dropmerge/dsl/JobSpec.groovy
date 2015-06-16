package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.JenkinsJob

/**
 * <p>A JobSpec represents a Jenkins job. A job has a name and runs on a specific Jenkins instance. Additionally, is can
 * have a textual description to specify more information on the job, which may not be speaking from the name.</p>
 *
 * <p>A job is typically constructed:
 * <pre>{@code
 * job 'job-name';
 * on buildmasterHYD;
 * description 'Tests ...'; }</pre>
 * The methods can be called in any order:
 * <pre>{@code
 * description 'Tests ...';
 * on buildmasterHYD;
 * job 'job-name'; }</pre>
 * Since the builder pattern is applied, the job could even be specifed on a single line:
 * <pre>{@code job 'job-name' on buildmasterHYD }</pre>
 * </p>
 *
 *
 */
class JobSpec {
    private Jenkins jenkinsInstance
    private String jobName
    private String description
    private Map<String,String> matrixAxes

    static final Jenkins jenkinsOfSVT = new Jenkins('http://srv-ind-svt9l.vanenburg.com:8080')

    static final Jenkins jenkinsOfCMT = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')

    static final Jenkins buildMasterHYD = new Jenkins('http://buildmaster-hyd.vanenburg.com/jenkins')

    static final Jenkins buildMasterNL = new Jenkins('http://buildmaster-nl.opentext.net/jenkins')

    /**
     * Set the Jenkins instance on which the job runs
     * @param instance
     * @return this
     */
    JobSpec on(Jenkins instance) { this.jenkinsInstance = instance; return this }

    /**
     * Set the job name
     * @param jobName
     * @return this
     */
    JobSpec job(String jobName) { this.jobName = jobName; return this }

    /**
     * Set a textual description for the job
     * @param description
     * @return this
     */
    JobSpec description(String description) { this.description = description; return this }

    /**
     * Set the matrix axes values
     * @param matrixAxes
     * @return this
     */
    JobSpec matrixValues(Map<String,String> matrixAxes) { this.matrixAxes = matrixAxes; return this }

    public JenkinsJob getJenkinsJob() {
        if (jenkinsInstance == null)
            throw new IllegalStateException('jenkinsInstance should not be null')
        if (jobName == null)
            throw new IllegalStateException('jobName should not be null')

        return jenkinsInstance.withJob(jobName, matrixAxes)
    }

    String getDescription() {
        return description
    }
}