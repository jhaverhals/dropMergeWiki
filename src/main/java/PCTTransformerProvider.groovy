import java.text.SimpleDateFormat

public class PCTTransformerProvider extends TransformerProvider {
    private static final BUILDMASTER_NL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    private static final JenkinsJob TRUNK_BVT_L = BUILDMASTER_NL.withJob('pct-trunk-build-installer-l-x64')
    private static final JenkinsJob TRUNK_CW_L = TRUNK_BVT_L
    private static final JenkinsJob TRUNK_MBV = BUILDMASTER_NL.withJob('pct-trunk-mb')
    private static final JenkinsJob TRUNK_PMD = TRUNK_BVT_L

    private static final JenkinsJob BVT_L = BUILDMASTER_NL.withJob('pct-trunk-wip-build-installer-l-x64')
    private static final JenkinsJob BVT_W = BUILDMASTER_NL.withJob('pct-trunk-wip-build-installer-w-x64')
    private static final JenkinsJob BVT_S = BUILDMASTER_NL.withJob('pct-trunk-wip-build-installer-s-x64')
    private static final JenkinsJob BVT_A = BUILDMASTER_NL.withJob('pct-trunk-wip-build-installer-a-x64')
    private static final JenkinsJob CW_L = BVT_L
    private static final JenkinsJob EW = BUILDMASTER_NL.withJob('security-eastwind')
    private static final JenkinsJob FRT_L = BUILDMASTER_NL.withJob('pct-trunk-wip-frt-l-x64')
    private static final JenkinsJob FRT_W = BUILDMASTER_NL.withJob('pct-trunk-wip-frt-w-x64')
    private static final JenkinsJob MBV = BUILDMASTER_NL.withJob('pct-trunk-wip-mb')
    private static final JenkinsJob PMD = BVT_L
    private static final JenkinsJob UPGRADE_L = BUILDMASTER_NL.withJob('pct-upgrade-trigger-l')
    private static final JenkinsJob UPGRADE_W = BUILDMASTER_NL.withJob('pct-upgrade-trigger-w')

    @Override
    Map<String, Closure<String>> getTransformer(UpdateWikiProperties props) {

        final String crucibleAuthToken = Crucible.getCrucibleAuthToken(props.crucibleUserName, props.cruciblePassword)
        final int openReviewCount = Crucible.getOpenReviewCount(props.crucibleProject, crucibleAuthToken)

        def transformers = [
                Team: { item -> CordysWiki.selectOption(item, 'Platform core') },
                DropMergeDate: { item ->
                    Calendar today = Calendar.getInstance();
                    int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
                    int daysUntilNextFriday = Calendar.FRIDAY - dayOfWeek;
                    if (daysUntilNextFriday < 0) {
                        daysUntilNextFriday = daysUntilNextFriday + 7;
                    }
                    Calendar nextFriday = (Calendar) today.clone();
                    nextFriday.add(Calendar.DAY_OF_WEEK, daysUntilNextFriday);
                    if (nextFriday.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
                        nextFriday.add(Calendar.DAY_OF_WEEK, 7);
                    }
                    return new SimpleDateFormat("yyyy-MM-dd 13:00:00").format(nextFriday.getTime());
                },

                ProductManagerName: { getUserLink('jpluimer', 'Johan Pluimers') },
                ArchitectName: { ' ' + getUserLink('wjgerrit', 'Willem Jan Gerritsen') },
                ScrumMasterName: { ' ' + getUserLink('gjansen', 'Gerwin Jansen') },

                FunctionalDescription: {
                    getJiraIssues('(sprint = \'' + props.sprintName + '\' OR sprint = \'PCT BOP 4.4 Sprint 5\') AND resolution = Fixed AND issuetype not in (\'Bug during story\', Todo)')
                },

                NewManualTestCases: { 'No' },
                NewManualTestCasesComment: withHtml { html -> html.p('No new manual tests added. We prefer automatic tests.') },

                ForwardPortingCompleted: { item -> CordysWiki.selectOption(item, 'Not applicable') },
                ForwardPortingCompletedComment: withHtml { html -> html.p('We always first fix in our own WIP.') },

                SuccesfulTestsAfter: { (BVT_L.getTestFigure(TestCount.Pass) as int) + (FRT_L.getTestFigureMultiConfig(TestCount.Pass) as int) },
                FailedTestsAfter: { (BVT_L.getTestFigure(TestCount.Fail) as int) + (FRT_L.getTestFigureMultiConfig(TestCount.Fail) as int) },

                MBViolationsHighBefore: { TRUNK_MBV.getMBFigure(WarningLevel.High) },
                MBViolationsHighAfter: { MBV.getMBFigure(WarningLevel.High) },
                MBViolationsMediumBefore: { TRUNK_MBV.getMBFigure(WarningLevel.Normal) },
                MBViolationsMediumAfter: { MBV.getMBFigure(WarningLevel.Normal) },

                CompilerWarningsBefore: { TRUNK_CW_L.compilerWarningFigure },
                CompilerWarningsAfter: { CW_L.compilerWarningFigure },
                /*CompilerWarningsComment: {
                    "We resolved " +
                            (((TRUNK_CW_L.compilerWarningFigure as int) + 10) - (CW_L.compilerWarningFigure as int)) +
                            ", and \"introduced\" 10 by deprecating a legacy API."
                }, */

                PMDViolationsHighBefore: { TRUNK_PMD.getPMDFigure(WarningLevel.High) },
                PMDViolationsHighAfter: { PMD.getPMDFigure(WarningLevel.High) },
                PMDViolationsMediumBefore: { TRUNK_PMD.getPMDFigure(WarningLevel.Normal) },
                PMDViolationsMediumAfter: { PMD.getPMDFigure(WarningLevel.Normal) },

                SuccessfulRegressionTestsComment: withTable { WikiTableBuilder table ->
                    table.setHeaders(['Type', 'OS', 'Successful', 'Failed', 'Skipped'])

                    int passCount = 0, failCount = 0, skipCount = 0
                    ['Linux': BVT_L, 'Windows': BVT_W, 'AIX': BVT_A, 'Solaris': BVT_S].each { String os, JenkinsJob job ->
                        passCount += job.getTestFigure(TestCount.Pass) as int
                        failCount += job.getTestFigure(TestCount.Fail) as int
                        skipCount += job.getTestFigure(TestCount.Skip) as int
                        table.addRow(['BVT', os, job.getTestFigure(TestCount.Pass), job.getTestFigure(TestCount.Fail), job.getTestFigure(TestCount.Skip)])
                    }

                    ['Linux': FRT_L, 'Windows': FRT_W].each { String os, JenkinsJob job ->
                        passCount += job.getTestFigureMultiConfig(TestCount.Pass) as int
                        failCount += job.getTestFigureMultiConfig(TestCount.Fail) as int
                        skipCount += job.getTestFigureMultiConfig(TestCount.Skip) as int
                        table.addRow(['FRT', os, job.getTestFigureMultiConfig(TestCount.Pass), job.getTestFigureMultiConfig(TestCount.Fail), job.getTestFigureMultiConfig(TestCount.Skip)])
                    }

                    table.addRow(['All', 'All', "$passCount", "$failCount", "$skipCount"])
                    return
                },
                FailedRegressionTestsComment: withHtml { html ->
                    html.p '⇧ The figures in the table above are not identical for all OSes ' +
                            'because of platform specific components and tests.'
                    html.hr()
                    html.p '⇦ The figures in the answer column regarding regression tests ' +
                            'are only the sum of Linux BVTs and FRTs. This leads to stable numbers, ' +
                            'and allows fair comparison of drop merge pages over time.'
                    html.hr()
                    html.p '⇩ The table below only shows differences in BVTs, ' +
                            'as FRTs are not comparable in our infrastructure.'
                },

                ReviewsDone: { item ->
                    return CordysWiki.selectOption(item, (openReviewCount == 0 ? 'Yes' : 'No'))
                },
                ReviewsDoneComment: {
                    getLink(Crucible.getBrowseReviewsURL(props.crucibleProject),
                            (openReviewCount > 0 ? "$openReviewCount open review(s)" : 'All reviews closed')
                    )
                },

                TotalRegressionTestsComment: withTable { table ->
                    def diffs = Jenkins.getTestDiffsPerSuite(TRUNK_BVT_L, BVT_L)

                    diffs.each { k, v ->
                        table.addRow('Suite / Test': k, 'Difference': String.format('%+d', v))
                    }
                    return
                },

                UpgradeTested: { item ->
                    def upgradeJob = UPGRADE_W
                    if (UPGRADE_W.lastBuildResult == 'SUCCESS' && UPGRADE_L.lastBuildResult == 'FAILURE')
                        upgradeJob = UPGRADE_L
                    selectOptionByStatus(item, upgradeJob, [SUCCESS: 'Yes', FAILURE: 'No'])
                },
                UpgradeTestedComment: withHtml { html ->
                    html.p {
                        a(href: UPGRADE_W.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Upgrade job')
                        mkp.yield ' from BOP 4.1 CU7.1 to latest wip.'
                        br()
                        a(href: UPGRADE_L.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Upgrade job')
                        mkp.yield ' from latest GA (BOP 4.3) to latest wip.'
                    }
                },

                IntegrationTestsPass: { item -> selectOptionByStatus(item, EW, [SUCCESS: 'Yes', FAILURE: 'No']) },
                IntegrationTestsPassComment: withHtml { html ->
                    html.p {
                        a(href: EW.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Eastwind successful')
                    }
                }
        ]

        transferFromPreviousPage(props, props.previousWikiDropMergePageId, ['SuccesfulTests', 'FailedTests'], transformers)

        return transformers
    }
}