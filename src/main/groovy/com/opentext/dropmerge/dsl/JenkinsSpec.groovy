package com.opentext.dropmerge.dsl

import groovy.time.TimeCategory
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

import org.jenkinsci.images.IconCSS

import com.opentext.dropmerge.*

class JenkinsSpec {

    private Map<String, Closure<String>> inputs

    JenkinsSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    void regressionTests(@DelegatesTo(TestTypesSpec) Closure jobsByType) {
        TestTypesSpec jobSpec = new TestTypesSpec()
        jobSpec.with jobsByType

        inputs['SuccesfulTestsBefore'] = {
            int total = jobSpec.comparableJobsByType.values().sum { Map<List<JobSpec>, List<JobSpec>> jobs ->
                jobs.values().sum { List<JobSpec> js ->
                    js.sum { JobSpec j ->
                        j.jenkinsJob.getTestFigure(TestCount.Pass) as int
                    }
                }
            }
            return "$total"
        }
        inputs['FailedTestsBefore'] = {
            int total = jobSpec.comparableJobsByType.values().sum { Map<List<JobSpec>, List<JobSpec>> jobs ->
                jobs.values().sum { List<JobSpec> js ->
                    js.sum { JobSpec j ->
                        j.jenkinsJob.getTestFigure(TestCount.Fail) as int
                    }
                }
            }
            return "$total"
        }
        inputs['SuccesfulTestsAfter'] = {
            int total = jobSpec.comparableJobsByType.values().sum { Map<List<JobSpec>, List<JobSpec>> jobs ->
                jobs.keySet().sum { List<JobSpec> js ->
                    js.sum { JobSpec j ->
                        j.jenkinsJob.getTestFigure(TestCount.Pass) as int
                    }
                }
            }
            return "$total"
        }
        inputs['FailedTestsAfter'] = {
            int total = jobSpec.comparableJobsByType.values().sum { Map<List<JobSpec>, List<JobSpec>> jobs ->
                jobs.keySet().sum { List<JobSpec> js ->
                    js.sum { JobSpec j ->
                        j.jenkinsJob.getTestFigure(TestCount.Fail) as int
                    }
                }
            }
            return "$total"
        }

        use(StringClosureCategories) {
            //TODO: This doesn't handle the com.opentext.dropmerge.dsl.ComparingJobsSpec.andJob construct
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
                                getJenkinsUrlWithStatus(job.jenkinsJob)
                        ])
                    }
                }

                table.addRow(['All', 'All', "$passCount", "$failCount", "$skipCount", ''])
                return
            }
/* This table is not used
            //TODO: This doesn't handle the com.opentext.dropmerge.dsl.ComparingJobsSpec.andJob construct
            inputs['SuccessfulRegressionTestsComment'] += TransformerProvider.withTable { WikiTableBuilder table ->
                jobSpec.comparableJobsByType.each { String type, Map<List<JobSpec>, List<JobSpec>> comparableJobs ->
                    comparableJobs.each { List<JobSpec> wips, List<JobSpec> trunks ->
                        String wipOS = wips*.description.unique().join(' / ');
                        trunks.each { JobSpec trunk ->
                            Date ts = trunk.jenkinsJob.getBuildTimestamp(JenkinsJob.LAST_COMPLETED_BUILD)
                            String timestampText = new SimpleDateFormat('MMM dd \'at\' HH:mm z').format(ts)
                            def diff = TimeCategory.minus(new Date(), ts).days
                            if(diff > 2)
                                timestampText += ", $diff days ago"
                            table.addRow('Type': type,
                                    'OS': wipOS,
                                    'WIP was compared to trunk job': getJenkinsUrlWithStatus(trunk.jenkinsJob),
                                    'Timestamp': timestampText
                            )
                        }
                    }
                }

                return
            }
*/
            inputs['SuccessfulRegressionTestsComment'] += TransformerProvider.withHtml { MarkupBuilder html ->
                html.style IconCSS.style
            }
        }

        inputs['FailedRegressionTestsComment'] = { jobSpec.extraComment.sb.toString() }

/* This table is not used
        //TODO: This doesn't handle the com.opentext.dropmerge.dsl.ComparingJobsSpec.andJob construct
        inputs['TotalRegressionTestsComment'] = TransformerProvider.withTable {
            table ->
                jobSpec.comparableJobsByType.each { String type, Map<List<JobSpec>, List<JobSpec>> comparableJobs ->
                    comparableJobs.each { List<JobSpec> wip, List<JobSpec> trunk ->
                        String wipOS = wip*.description.unique().join(' / ')
                        Jenkins.getTestDiffsPerSuite(trunk*.jenkinsJob, wip*.jenkinsJob).each { k, v ->
                            table.addRow(
                                    'Suite / Test': k,
                                    'Difference': String.format('%+d', v),
                                    'Type': type,
                                    'OS': wipOS,
                                    'Justification': jobSpec.justifications[type][wip.first()]?.getJustificationsForClassName(k)
                            )
                        }
                    }
                }
        }
*/
    }

    void pmd(@DelegatesTo(ComparableJobsSpec) Closure jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['PMDViolationsHighBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.Normal) }
        inputs['PMDViolationsHighAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.Normal) }

        use(StringClosureCategories) {
            [HIGH: 'High', NORMAL: 'Medium'].each { String jenkinsTerm, String wikiFieldTerm ->
                inputs["PMDViolations${wikiFieldTerm}Comment"] = createQualityMetricComment(jobSpec, "pmdResult/$jenkinsTerm", 'PMD results')
                inputs["PMDViolations${wikiFieldTerm}Comment"] += TransformerProvider.withTable { table ->
                    Jenkins.DifferenceDetails differenceDetails = Jenkins.getDetailedPMDDiffsPerSuite(jobSpec.trunk, jobSpec.wip, [jenkinsTerm])
                    differenceDetails.diffsPerSuite.each buildDiffTable(table, differenceDetails, "pmdResult/$jenkinsTerm", jobSpec)
                }
            }
        }
    }

    private Closure buildDiffTable(WikiTableBuilder table, Jenkins.DifferenceDetails diffDetails, String reportUrl, ComparableJobsSpec jobSpec) {
        return { k, v ->
            String linkTrunk, linkWip
            boolean b2a, a2b;

            if ((b2a = diffDetails.beforeToAfter.containsKey(k)) || diffDetails.onlyBefore.contains(k)) {
                linkTrunk = getFileReportUrl(jobSpec.trunk, reportUrl, k)
                if (b2a)
                    linkWip = getFileReportUrl(jobSpec.wip, reportUrl, diffDetails.beforeToAfter[k])
            } else if ((a2b = diffDetails.afterToBefore.containsKey(k)) || diffDetails.onlyAfter.contains(k)) {
                linkWip = getFileReportUrl(jobSpec.wip, reportUrl, k)
                if (a2b)
                    linkTrunk = getFileReportUrl(jobSpec.trunk, reportUrl, diffDetails.afterToBefore[k])
            }

            table.addRow(
                    'File': k,
                    'Link': {
                        nobr {
                            if (linkTrunk)
                                a(href: linkTrunk, 'T')
                            if (linkTrunk && linkWip)
                                mkp.yield ' / '
                            if (linkWip)
                                a(href: linkWip, 'W')
                        }
                    },
                    'Diff': String.format('%+d', v))
        }
    }

    private String getFileReportUrl(JenkinsJob job, String reportUrl, String fileName) {
        return "${job.getBuildUrl(JenkinsJob.LAST_SUCCESSFUL_BUILD)}/$reportUrl/file.${fileName.hashCode()}/"
    }

    void compilerWarnings(@DelegatesTo(ComparableJobsSpec) Closure jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['CompilerWarningsBefore'] = { jobSpec.trunk.compilerWarningFigure }
        inputs['CompilerWarningsAfter'] = { jobSpec.wip.compilerWarningFigure }

        use(StringClosureCategories) {
            inputs['CompilerWarningsComment'] = createQualityMetricComment(jobSpec, 'warningsResult', 'Compile Warning results')
            inputs['CompilerWarningsComment'] += TransformerProvider.withTable { table ->
                Jenkins.DifferenceDetails differenceDetails = Jenkins.getDetailedCompilerWarningsDiffsPerSuite(jobSpec.trunk, jobSpec.wip)
                differenceDetails.diffsPerSuite.each buildDiffTable(table, differenceDetails, 'warningsResult', jobSpec)
            }
        }
    }

    void mbv(@DelegatesTo(ComparableJobsSpec) Closure jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['MBViolationsHighBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.Normal) }
        inputs['MBViolationsHighAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.Normal) }

        use(StringClosureCategories) {
            [HIGH: 'High', NORMAL: 'Medium'].each { String jenkinsTerm, String wikiFieldTerm ->
                inputs["MultibrowserViolations${wikiFieldTerm}Comment"] = createQualityMetricComment(jobSpec, "muvipluginResult/$jenkinsTerm", 'MBV results')
                inputs["MultibrowserViolations${wikiFieldTerm}Comment"] += TransformerProvider.withTable { table ->
                    Jenkins.DifferenceDetails differenceDetails = Jenkins.getDetailedMBVDiffsPerSuite(jobSpec.trunk, jobSpec.wip, [jenkinsTerm])
                    differenceDetails.diffsPerSuite.each buildDiffTable(table, differenceDetails, "muvipluginResult/$jenkinsTerm", jobSpec)
                }
            }
        }
    }

    private static Closure<String> createQualityMetricComment(ComparableJobsSpec jobPairSpec, String reportUrl, String reportTitle) {
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

    void upgrade(@DelegatesTo(JobsSpec) Closure jobsClosure) {
        JobsSpec jobsSpec = new JobsSpec()
        jobsSpec.with jobsClosure;

        List<JobSpec> jobs = jobsSpec.jobs
        inputs['UpgradeTested'] = { item ->
            CordysWiki.selectOption(item, (jobs.every { JobSpec j -> j.jenkinsJob.lastBuildResult == 'SUCCESS' } ? 'Yes' : 'No'))
        }
        inputs['UpgradeTestedComment'] = TransformerProvider.withHtml { html ->
            html.p {
                jobs.each { JobSpec j ->
                    getJenkinsUrlWithStatus(j.jenkinsJob, JenkinsJob.LAST_COMPLETED_BUILD, 'Upgrade job').with {
                        it.delegate = html
                        it.call()
                    }
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }

    void integrationTests(@DelegatesTo(JobsSpec) Closure jobsClosure) {
        JobsSpec jobsSpec = new JobsSpec()
        jobsSpec.with jobsClosure;

        List<JobSpec> jobs = jobsSpec.jobs
        inputs['IntegrationTestsPass'] = { item ->
            CordysWiki.selectOption(item, (jobs.every { JobSpec j -> j.jenkinsJob.lastBuildResult == 'SUCCESS' } ? 'Yes' : 'No'))
        }
        inputs['IntegrationTestsPassComment'] = TransformerProvider.withHtml { html ->
            html.p {
                jobs.each { JobSpec j ->
                    getJenkinsUrlWithStatus(j.jenkinsJob, JenkinsJob.LAST_COMPLETED_BUILD, 'Integration test job').with {
                        it.delegate = html
                        it.call()
                    }
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }

    private Closure getJenkinsUrl(JenkinsJob job, String build = null, String linkText = null) {
        return { a(href: job.getBuildUrl(build), linkText ?: job.toString()) }
    }

    private Closure getJenkinsUrlWithStatus(JenkinsJob job, String build = null, String linkText = null) {
        return { span(class: "jenkinsJobStatus jenkinsJobStatus_${job.color}", getJenkinsUrl(job, build, linkText)) }
    }

}
