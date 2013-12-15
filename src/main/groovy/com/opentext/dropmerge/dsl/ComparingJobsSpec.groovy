package com.opentext.dropmerge.dsl

import java.util.regex.Pattern

/**
 * <p>A ComparingJobsSpec specifies a list job, of which some can be compared to each other. For example, one might
 * specify some jobs that perform unit test. Global jobs could be compared to team's wip jobs.</p>
 *
 * <p>The list of jobs is typically specified:
 * <pre>
 * withJob { job 'pct-frt-linux' on buildMasterNL; description 'All PCT FRT tests on Linux' }
 * comparedToJob { job 'PCT-L' on jenkinsOfSVT; description 'All PCT FRT tests on Linux' }
 * withJob { job 'pct-frt-aix' on buildMasterNL; description 'All PCT FRT tests on AIX' }
 * </pre></p>
 *
 * <p>Not all items in the list are necesarrily comparable. In the example above, the AIX job will not be compared to
 * any other job. One can specify multiple jobs in a 'withJob' succesively.
 * None of those are considered comparable. Only when specifying a job with 'comparedToJob', this job will be considered
 * comparable to the previous job(s). To unite several jobs for comparison, the 'andJob' method can be used:</p>
 *
 * <pre>
 * withJob { job 'pct-frt-junit' on buildMasterNL; description 'All PCT JUnit tests on Windows and Linux' }
 * andJob { job 'pct-frt-soapunit' on buildMasterNL; description 'All PCT soapunit tests on Windows and Linux' }
 * andJob { job 'pct-frt-soapui' on buildMasterHYD; description 'All PCT SoapUI tests on Windows and Linux' }
 * comparedToJob { job 'PCT-L' on jenkinsOfSVT; description 'All PCT FRT tests on Linux' }
 * andJob { job 'PCT-W' on jenkinsOfSVT; description 'All PCT FRT tests on Windows' }
 * </pre>
 *
 */
class ComparingJobsSpec extends JobsSpec {
    private Map<JobSpec, JobSpec> comparableJobSpecs = new LinkedHashMap<>()
    private Map<JobSpec, DifferencesSpec> justifications = new HashMap<>()

    private Map<JobSpec, List<JobSpec>> linkedJobSpecs = new HashMap<>()
    private JobSpec lastComparingJob = null

    @Override
    def withJob(@DelegatesTo(JobSpec) trunkJob) {
        lastComparingJob = null
        return super.withJob(trunkJob)
    }

    def comparedToJob(@DelegatesTo(JobSpec) job) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with job

        if (comparableJobSpecs.containsKey(this.jobs.last()))
            throw new IllegalArgumentException('Can only compare jobs one-on-one.')
        comparableJobSpecs[this.jobs.last()] = jobSpec;
        lastComparingJob = jobSpec
    }

    def andJob(@DelegatesTo(JobSpec) trunkJob) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with trunkJob

        JobSpec additionalFor = this.lastComparingJob ?: this.jobs.last()

        if (linkedJobSpecs.containsKey(additionalFor)) {
            this.linkedJobSpecs[additionalFor] += [jobSpec]
        } else {
            this.linkedJobSpecs[additionalFor] = [jobSpec]
        }
    }

    def differences(@DelegatesTo(DifferencesSpec) diff) {
        DifferencesSpec diffSpec = new DifferencesSpec()
        diffSpec.with diff

        justifications[this.jobs.last()] = diffSpec;
    }

    class DifferencesSpec {
        Map<Pattern, String> patternStringMap = new LinkedHashMap<>()
        Pattern tempPattern

        DifferencesSpec matching(Pattern pattern) {
            this.tempPattern = pattern
            return this
        }

        void areJustifiedBecause(String message) {
            assert tempPattern != null
            patternStringMap[tempPattern] = message
            tempPattern = null
        }

        void allAreJustifiedBecause(String message) {
            assert tempPattern == null
            matching(~/^.*$/).areJustifiedBecause(message)
        }

        String getJustificationsForClassName(String className) {
            StringBuilder sb = new StringBuilder()
            patternStringMap.each { Pattern p, String s ->
                if (p.matcher(className).matches())
                    sb.append(s).append(' ')
            }
            return sb.toString()
        }
    }


    Map<JobSpec, JobSpec> getComparableJobSpecs() {
        return comparableJobSpecs
    }

    Map<JobSpec, DifferencesSpec> getJustifications() {
        return justifications
    }

    List<JobSpec> getJobSpecPlusLinkedJobSpecs(JobSpec a) {
        if (!linkedJobSpecs.containsKey(a))
            return [a]
        return [a] + linkedJobSpecs[a]
    }
}
