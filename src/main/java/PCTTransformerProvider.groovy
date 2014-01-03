import com.opentext.dropmerge.dsl.DropMergeInput

DropMergeInput.provide {
    team {
        name 'Platform core'
        scrumMaster 'Gerwin Jansen', 'gjansen'
        architect 'Willem Jan Gerritsen', 'wjgerrit'
        productManager 'Johan Pluimers', 'jpluimer'
        otherMembers 'astellingwerf', 'dkwakkel', 'wvplagge'
    }

    dropMergeOn every.odd.friday.includingToday

    functionalDescription {
        // withText 'We have done stuff:'
        withJiraIssuesTable "sprint = '${myProperties['sprintName']}' AND resolution = Fixed AND issuetype not in ('Bug during story', Todo)"
        // withHtml { html -> html.i 'That\'s what we\'ve done!' }
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
                    endingWith '.SubroleDeletingUpgradeStepTest' areJustifiedBecause 'SVT doesn\'t run this test yet.'
                    matching ~/^com\.eibus\.sso\.authentication\.audit\.HttpURLConnectionBaseTest$/ areJustifiedBecause 'Test has been renamed to conform with SVT patterns.'
                    equalTo 'com.eibus.util.system.win32.WindowsRegistryTest' areJustifiedBecause 'SVT uses an old version of Ant, where skipped tests are considered successful.'
                }

                withJob { job 'pct-trunk-wip-frt-w-x64' on buildMasterNL; description 'Windows' }
//                comparedToJob { job 'PlatformCore-W' on jenkinsOfSVT; description 'Windows' }
            }
            /*ofType('UIUnit') {
                Jenkins globalUIUnits = new Jenkins('http://cin9002:8080')

                withJob {
                    job 'pct-trunk-wip-uiunit' on buildMasterNL matrixValues component: 'adminui';
                    description 'intadminui'
                }
                comparedToJob { job 'Admin-FITs-Trunk' on globalUIUnits }
                andJob { job 'Admin-WithoutFITs-Trunk' on globalUIUnits }

                withJob {
                    job 'pct-trunk-wip-uiunit' on buildMasterNL matrixValues component: 'webgateway';
                    description 'webgateway'
                }
                comparedToJob { job 'WebGateway-Trunk' on globalUIUnits }

                withJob {
                    job 'pct-trunk-wip-uiunit' on buildMasterNL matrixValues component: 'artifactaudit';
                    description 'audit'
                }
                comparedToJob { job 'Audit-Trunk' on globalUIUnits }

                withJob {
                    job 'pct-trunk-wip-uiunit' on buildMasterNL matrixValues component: 'ldapconn';
                    description 'ldapconn'
                }
                comparedToJob { job 'LDAPConn-Trunk' on globalUIUnits }
            } */
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
        newManualTestCasesAdded no, 'No new manual tests added. We prefer automated tests.'
        forwardPortingCompleted notApplicable, 'We always first fix in our own WIP.'
        securityIssuesIntroduced no, 'Guarded by automated ACL tests and in code reviews.'
				
    }
}