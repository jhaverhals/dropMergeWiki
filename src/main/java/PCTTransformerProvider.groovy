import static Jenkins.*
import static TestCount.*
import static WarningLevel.High
import static WarningLevel.Normal

public class PCTTransformerProvider extends TransformerProvider {

    private static final String BVT_L = 'pct-trunk-wip-build-installer-l-x64'
    private static final String BVT_W = 'pct-trunk-wip-build-installer-w-x64'
    private static final String BVT_S = 'pct-trunk-wip-build-installer-s-x64'
    private static final String BVT_A = 'pct-trunk-wip-build-installer-a-x64'
    private static final String FRT_L = 'pct-trunk-wip-frt-l-x64'
    private static final String FRT_W = 'pct-trunk-wip-frt-w-x64'

    @Override
    Map<String, Closure<String>> getTransformer(Properties props) {
        def transformers = [
//                Team: { item -> selectOption(item, 'Platform core') },
//
//                ProductManagerName: { getUserLink('jpluimer', 'Johan Pluimers') },
//                ArchitectName: { ' ' + getUserLink('wjgerrit', 'Willem Jan Gerritsen') },
//                ScrumMasterName: { ' ' + getUserLink('gjansen', 'Gerwin Jansen') },

//                FunctionalDescription: {
//                  getJiraIssues('sprint = \'PCT BOP 4.3 Sprint 6\' AND resolution = Fixed AND issuetype not in (\'Bug during story\', Todo)')
//                },

//                NewManualTestCases: { 'No' },
//                NewManualTestCasesComment: withHtml { html -> html.p('No new manual tests added. We prefer automatic tests.') },
//
//                ForwardPortingCompleted: { item -> selectOption(item, 'Not applicable') },
//                ForwardPortingCompletedComment: withHtml { html -> html.p('We always first fix in our own WIP.') },
//
                SuccesfulTestsBefore: { getTestFigure(props.jenkinsTrunkBVTJob, Pass, Skip) },
                SuccesfulTestsAfter: { getTestFigure(props.jenkinsWipBVTJob, Pass, Skip) },
                FailedTestsBefore: { getTestFigure(props.jenkinsTrunkBVTJob, Fail) },
                FailedTestsAfter: { getTestFigure(props.jenkinsWipBVTJob, Fail) },

                MBViolationsHighBefore: { getMBFigure(props.jenkinsTrunkMBJob, High) },
                MBViolationsHighAfter: { getMBFigure(props.jenkinsWipMBJob, High) },
                MBViolationsMediumBefore: { getMBFigure(props.jenkinsTrunkMBJob, Normal) },
                MBViolationsMediumAfter: { getMBFigure(props.jenkinsWipMBJob, Normal) },

                CompilerWarningsBefore: { getCompilerWarningFigure(props.jenkinsTrunkCWJob) },
                CompilerWarningsAfter: { getCompilerWarningFigure(props.jenkinsWipCWJob) },

                PMDViolationsHighBefore: { getPMDFigure(props.jenkinsTrunkPMDJob, High) },
                PMDViolationsHighAfter: { getPMDFigure(props.jenkinsWipPMDJob, High) },
                PMDViolationsMediumBefore: { getPMDFigure(props.jenkinsTrunkPMDJob, Normal) },
                PMDViolationsMediumAfter: { getPMDFigure(props.jenkinsWipPMDJob, Normal) },

                SuccessfulRegressionTestsComment: withTable { WikiTableBuilder table ->
                    table.setHeaders(['Type', 'OS', 'Successful', 'Failed', 'Skipped'])

                    ['Linux': BVT_L, 'Windows': BVT_W, 'AIX': BVT_A, 'Solaris': BVT_S].each { String os, String job ->
                        table.addRow(['BVT', os, getTestFigure(job, Pass), getTestFigure(job, Fail), getTestFigure(job, Skip)])
                    }

                    ['Linux': FRT_L, 'Windows': FRT_W].each { String os, String job ->
                        table.addRow(['FRT', os, getTestFigure(job, Pass), getTestFigure(job, Fail), getTestFigure(job, Skip)][])
                    }
                },

                ReviewsDone: { item ->
                    int openReviewCount = Crucible.getOpenReviewCount(props.crucibleProject, Crucible.getCrucibleAuthToken(props.crucibleUserName, props.cruciblePassword))
                    return CordysWiki.selectOption(item, (openReviewCount == 0 ? 'Yes' : 'No'))
                },
                ReviewsDoneComment: withHtml { html ->
                    int openReviewCount = Crucible.getOpenReviewCount(props.crucibleProject, Crucible.getCrucibleAuthToken(props.crucibleUserName, props.cruciblePassword))
                    html.a(href: Crucible.getBrowseReviewsURL(props.crucibleProject),
                            (openReviewCount > 0 ? "$openReviewCount open review(s)" : 'All reviews closed')
                    )
                },

                NewAutomatedTestCases: withTable { table ->
                    def diffs = Jenkins.getTestDiffsPerSuite(props.jenkinsTrunkBVTJob, props.jenkinsWipBVTJob)

                    diffs.each { k, v ->
                        table.addRow('Suite / Test': k, 'Difference': String.format('%+d', v))
                    }
                },

                UpgradeTested: { item -> selectOptionByStatus(item, props.jenkinsWipUpgradeJob, [SUCCESS: 'Yes', FAILURE: 'No']) },
                UpgradeTestedComment: withHtml { html ->
                    html.p {
                        a(href: 'http://buildmaster-nl.vanenburg.com/jenkins/job/' + props.jenkinsWipUpgradeJob + '/lastCompletedBuild/', 'Upgrade job')
                        mkp.yield ' from BOP 4.1 CU7.1 to latest wip.'
                    }
                },

                IntegrationTestsPass: { item -> selectOptionByStatus(item, props.jenkinsWipEastWindJob, [SUCCESS: 'Yes', FAILURE: 'No']) },
        ]
        return transformers
    }
}