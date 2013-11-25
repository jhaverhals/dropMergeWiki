package com.opentext.dropmerge.dsl

import java.util.regex.Pattern


class ComparingJobsSpec extends JobsSpec {
    private Map<JobSpec, JobSpec> comparableJobSpecs = new HashMap<>()
    private Map<JobSpec, DifferencesSpec> justifications = new HashMap<>()

    def comparedToJob(@DelegatesTo(JobSpec) job) {
        JobSpec jobSpec = new JobSpec()
        jobSpec.with job

        if (comparableJobSpecs.containsKey(this.jobs.last()))
            throw new IllegalArgumentException('Can only compare jobs one-on-one.')
        comparableJobSpecs[this.jobs.last()] = jobSpec;
    }

    def differences(@DelegatesTo(DifferencesSpec) diff) {
        DifferencesSpec diffSpec = new DifferencesSpec()
        diffSpec.with diff

        justifications[this.jobs.last()] = diffSpec;
    }

    class DifferencesSpec {
        Map<Pattern, String> patternStringMap = new HashMap<>()
        List<Pattern> orderedPatterns = []
        Pattern tempPattern

        DifferencesSpec matching(Pattern pattern) {
            this.tempPattern = pattern
            return this
        }

        void areJustifiedBecause(String message) {
            assert tempPattern != null
            orderedPatterns.add(tempPattern)
            patternStringMap[tempPattern] = message
            tempPattern = null
        }

        void allAreJustifiedBecause(String message) {
            assert tempPattern == null
            matching(~/^.*$/).areJustifiedBecause(message)
        }

        String getJustificationsForClassName(String className) {
            StringBuilder sb = new StringBuilder()
            orderedPatterns.each { Pattern p ->
                if(p.matcher(className).matches())
                    sb.append(patternStringMap[p]).append(' ')
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
}
