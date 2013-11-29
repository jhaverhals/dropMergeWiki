package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.*

class JenkinsSpec {

    private Map<String, Closure<String>> inputs

    JenkinsSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    static final Jenkins jenkinsOfSVT = new Jenkins('http://srv-ind-svt9l.vanenburg.com:8080')

    static final Jenkins jenkinsOfCMT = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')

    static final Jenkins buildMasterHYD = new Jenkins('http://buildmaster-hyd.vanenburg.com/jenkins')

    static final Jenkins buildMasterNL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    def regressionTests(@DelegatesTo(TestTypesSpec) jobsByType) {
        TestTypesSpec jobSpec = new TestTypesSpec()
        jobSpec.with jobsByType

        inputs['SuccesfulTestsBefore'] = {
            int total = 0
            jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> jobs ->
                jobs.each { JobSpec ignore, JobSpec j ->
                    total += j.jenkinsJob.getTestFigure(TestCount.Pass) as int }
            }
            return "$total"
        }
        inputs['FailedTestsBefore'] = {
            int total = 0
            jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> jobs ->
                jobs.each { JobSpec ignore, JobSpec j ->
                    total += j.jenkinsJob.getTestFigure(TestCount.Fail) as int }
            }
            return "$total"
        }
        inputs['SuccesfulTestsAfter'] = {
            int total = 0
            jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> jobs ->
                jobs.each { JobSpec j, JobSpec ignore ->
                    total += j.jenkinsJob.getTestFigure(TestCount.Pass) as int }
            }
            return "$total"
        }
        inputs['FailedTestsAfter'] = {
            int total = 0
            jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> jobs ->
                jobs.each { JobSpec j, JobSpec ignore ->
                    total += j.jenkinsJob.getTestFigure(TestCount.Fail) as int }
            }
            return "$total"
        }

        use(StringClosureCategories) {
            inputs['SuccessfulRegressionTestsComment'] = TransformerProvider.withTable { WikiTableBuilder table ->
                table.setHeaders(['Type', 'OS', 'Successful', 'Failed', 'Skipped', 'Link'])

                int passCount = 0, failCount = 0, skipCount = 0
                jobSpec.jobsByType.each { String type, List<JobSpec> jobs ->
                    jobs.each { JobSpec job ->
                        passCount += job.jenkinsJob.getTestFigure(TestCount.Pass) as int
                        failCount += job.jenkinsJob.getTestFigure(TestCount.Fail) as int
                        skipCount += job.jenkinsJob.getTestFigure(TestCount.Skip) as int
                        table.addRow([
                                type,
                                job.description,
                                job.jenkinsJob.getTestFigure(TestCount.Pass),
                                job.jenkinsJob.getTestFigure(TestCount.Fail),
                                job.jenkinsJob.getTestFigure(TestCount.Skip),
                                { a(href: job.jenkinsJob.jobUrl, job.jenkinsJob) }
                        ])
                    }
                }

                table.addRow(['All', 'All', "$passCount", "$failCount", "$skipCount", ''])
                return
            }
            inputs['SuccessfulRegressionTestsComment'] += TransformerProvider.withTable { WikiTableBuilder table ->
                jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> comparableJobs ->
                    comparableJobs.each { JobSpec wip, JobSpec trunk ->
                        table.addRow('Type': type,
                                'OS': wip.description,
                                'WIP was compared to trunk job': { a(href: trunk.jenkinsJob.jobUrl, trunk.jenkinsJob) }
                        )
                    }
                }

                return
            }
        }

        inputs['TotalRegressionTestsComment'] = TransformerProvider.withTable {
            table ->
                jobSpec.comparableJobsByType.each { String type, Map<JobSpec, JobSpec> comparableJobs ->
                    comparableJobs.each { JobSpec wip, JobSpec trunk ->
                        Jenkins.getTestDiffsPerSuite(trunk.jenkinsJob, wip.jenkinsJob).each { k, v ->
                            table.addRow(
                                    'Suite / Test': k,
                                    'Difference': String.format('%+d', v),
                                    'Type': type,
                                    'Justification': jobSpec.justifications[type][wip]?.getJustificationsForClassName(k)
                            )
                        }
                    }
                }
        }
    }

    def pmd(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['PMDViolationsHighBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.Normal) }
        inputs['PMDViolationsHighAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.Normal) }

        use(StringClosureCategories) {
            inputs['PMDViolationsHighComment'] = createQualityMetricComment(jobSpec, 'pmdResult/HIGH', 'PMD results')
            inputs['PMDViolationsHighComment'] += TransformerProvider.withTable { table ->
                Jenkins.getPMDDiffsPerSuite(jobSpec.trunk, jobSpec.wip, ['HIGH']).each { k, v ->
                    table.addRow('File': k, 'Difference': String.format('%+d', v))
                }
            }

            inputs['PMDViolationsMediumComment'] = createQualityMetricComment(jobSpec, 'pmdResult/NORMAL', 'PMD results')
            inputs['PMDViolationsMediumComment'] += TransformerProvider.withTable { table ->
                Jenkins.getPMDDiffsPerSuite(jobSpec.trunk, jobSpec.wip, ['NORMAL']).each { k, v ->
                    table.addRow('File': k, 'Difference': String.format('%+d', v))
                }
            }
        }
    }

    def compilerWarnings(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['CompilerWarningsBefore'] = { jobSpec.trunk.compilerWarningFigure }
        inputs['CompilerWarningsAfter'] = { jobSpec.wip.compilerWarningFigure }

        inputs['CompilerWarningsComment'] = createQualityMetricComment(jobSpec, 'warnings2Result', 'Compile Warning results')
    }

    def mbv(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['MBViolationsHighBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.Normal) }
        inputs['MBViolationsHighAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.Normal) }

        inputs['MultibrowserViolationsHighComment'] = createQualityMetricComment(jobSpec, 'muvipluginResult/HIGH', 'MBV results')
        inputs['MultibrowserViolationsMediumComment'] = createQualityMetricComment(jobSpec, 'muvipluginResult/NORMAL', 'MBV results')
    }

    static Closure<String> createQualityMetricComment(ComparableJobsSpec jobPairSpec, String reportUrl, String reportTitle) {
        return TransformerProvider.withHtml { html ->
            html.p {
                html.b 'Trunk:'
                html.mkp.yieldUnescaped '&nbsp;'
                html.a(href: jobPairSpec.trunk.getBuildUrl(JenkinsJob.LAST_SUCCESSFUL_BUILD) + '/' + reportUrl + '/', reportTitle)
                html.mkp.yield ' from our own trunk build.'
                html.br()
                html.b 'WIP:'
                html.mkp.yieldUnescaped '&nbsp;'
                html.a(href: jobPairSpec.wip.getBuildUrl(JenkinsJob.LAST_SUCCESSFUL_BUILD) + '/' + reportUrl + '/', reportTitle)
                html.mkp.yield ' from our WIP build.'
            }
        }
    }

    def upgrade(@DelegatesTo(JobsSpec) jobsClosure) {
        JobsSpec jobsSpec = new JobsSpec()
        jobsSpec.with jobsClosure;

        List<JobSpec> jobs = jobsSpec.jobs
        inputs['UpgradeTested'] = { item ->
            CordysWiki.selectOption(item, (jobs.every { JobSpec j -> j.jenkinsJob.lastBuildResult == 'SUCCESS' } ? 'Yes' : 'No'))
        }
        inputs['UpgradeTestedComment'] = TransformerProvider.withHtml { html ->
            html.p {
                jobs.each { JobSpec j ->
                    html.a(href: j.jenkinsJob.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Upgrade job')
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }

    def integrationTests(@DelegatesTo(JobsSpec) jobsClosure) {
        JobsSpec jobsSpec = new JobsSpec()
        jobsSpec.with jobsClosure;

        List<JobSpec> jobs = jobsSpec.jobs
        inputs['IntegrationTestsPass'] = { item ->
            CordysWiki.selectOption(item, (jobs.every { JobSpec j -> j.jenkinsJob.lastBuildResult == 'SUCCESS' } ? 'Yes' : 'No'))
        }
        inputs['IntegrationTestsPassComment'] = TransformerProvider.withHtml { html ->
            html.p {
                jobs.each { JobSpec j ->
                    html.a(href: j.jenkinsJob.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Integration test job')
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }

}
