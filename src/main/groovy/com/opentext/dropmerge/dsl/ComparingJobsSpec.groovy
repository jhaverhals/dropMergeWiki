package com.opentext.dropmerge.dsl

import java.util.regex.Pattern


class ComparingJobsSpec extends JobsSpec {
    private Map<JobSpec, JobSpec> comparableJobSpecs = new HashMap<>()
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
                if(p.matcher(className).matches())
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
         if(!linkedJobSpecs.containsKey(a))
             return [a]
        return [a] + linkedJobSpecs[a]
    }
}
