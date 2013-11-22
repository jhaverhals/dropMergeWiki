import com.opentext.dropmerge.TransformerProvider
import com.opentext.dropmerge.UpdateWikiProperties
import com.opentext.dropmerge.dsl.DropMergeInput

public class PCTTransformerProvider extends TransformerProvider {

    @Override
    Map<String, Closure<String>> getTransformer(UpdateWikiProperties props) {
        def transformers = DropMergeInput.provide {
            team 'Platform core'
            scrumMaster 'Gerwin Jansen', 'gjansen'
            architect 'Willem Jan Gerritsen', 'wjgerrit'
            productManager 'Johan Pluimers', 'jpluimer'

            dropMergeOn nextOdd.friday
            goToCCB today.orNextOdd.thursday

            functionalDescription {
                withText 'We have done stuff:'
                withJiraIssuesTable "sprint = '${myProperties['sprintName']}' AND resolution = Fixed AND issuetype not in ('Bug during story', Todo)"
                withHtml { html -> html.i 'That\'s what we\'ve done!' }
            }

            wiki {
                userName myProperties['wikiUserName']
                password myProperties['wikiPassword']
                pageId myProperties['wikiDropMergePageId']
            }

            crucible {
                userName myProperties['crucibleUserName']
                password myProperties['cruciblePassword']
                projectKey 'SEC'
            }

            jenkins {
                regressionTests {
                    ofType('BVT') {
                        withJob { job 'pct-trunk-wip-build-installer-l-x64' on buildMasterNL; description 'Linux' }
                        comparedToJob { job 'Trunk-Lin64-Java7' on jenkinsOfCMT; description 'Linux' }

                        withJob { job 'pct-trunk-wip-build-installer-w-x64' on buildMasterNL; description 'Windows' }
                        withJob { job 'pct-trunk-wip-build-installer-a-x64' on buildMasterNL; description 'AIX' }
                        withJob { job 'pct-trunk-wip-build-installer-s-x64' on buildMasterNL; description 'Solaris' }
                    }
                    ofType('FRT') {
                        withJob { job 'pct-trunk-wip-frt-l-x64' on buildMasterNL; description 'Linux' }
                        comparedToJob { job 'PlatformCore-L' on jenkinsOfSVT; description 'Linux' }
                        differences {
                            allAreJustifiedBecause 'Difference due to (job) configuration:'
                            matching ~/com\.cordys\.cap\.PlatformCoreSuite/ areJustifiedBecause 'SVT doesn\'t run this suite yet.'
                            matching ~/com\.eibus\.web\.soap\.(XGateway|RedirectingSOAPTransaction)Test/ areJustifiedBecause 'SVT runs this test which isn\'t ours.'
                            matching ~/com\.eibus\.applicationconnector\.event\.Eventservice_Prerequisites/ areJustifiedBecause 'We don\'t run with test: It seems worthless.'
                            matching ~/^.*SubroleDeletingUpgradeStepTest$/ areJustifiedBecause 'Test is not in bcptests.zip yet. Will be fixed with this drop merge.'
                            matching ~/^com\.eibus\.sso\.authentication\.audit\..*/ areJustifiedBecause 'Test is not in bcptests.zip yet. Will be fixed with this drop merge.'
                        }

                        withJob { job 'pct-trunk-wip-frt-w-x64' on buildMasterNL; description 'Windows' }
                    }
                }
                pmd {
                    trunk { job 'pct-trunk-build-installer-l-x64' on buildMasterNL }
                    wip { job 'pct-trunk-wip-build-installer-l-x64' on buildMasterNL }
                }
                compilerWarnings {
                    trunk { job 'pct-trunk-build-installer-l-x64' on buildMasterNL }
                    wip { job 'pct-trunk-wip-build-installer-l-x64' on buildMasterNL }
                }
                mbv {
                    trunk { job 'pct-trunk-mb' on buildMasterNL }
                    wip { job 'pct-trunk-wip-mb' on buildMasterNL }
                }

                upgrade {
                    withJob {
                        job 'pct-upgrade-trigger-w' on buildMasterNL;
                        description 'from BOP 4.1 CU7.1 to latest wip.'
                    }
                    withJob {
                        job 'pct-upgrade-trigger-l' on buildMasterNL;
                        description 'from latest GA (BOP 4.3.1) to latest wip.'
                    }
                }
                integrationTests {
                    withJob {
                        job 'security-eastwind' on buildMasterNL;
                        description 'running Eastwind against latest wip.'
                    }
                }
            }

            qualityAndProcessQuestions {
                newManualTestCassesAdded 'No', 'No new manual tests added. We prefer automated tests.'
                completedForwardPorting notApplicable, 'We always first fix in our own WIP.'
                introducedSecurityIssues no, 'Guarded by automated ACL tests and in code reviews.'
            }
        }.inputs

        return transformers
    }
}