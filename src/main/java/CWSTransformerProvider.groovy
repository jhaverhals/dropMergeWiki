import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.dsl.DropMergeInput

class CWSTransformerProvider {

    public static void main(String[] args) {
        DropMergeInput.provide {
            team {
                name 'CWS'
                productManager 'hkastenb'
                architect 'rprins', 'awisse', 'gjlubber'
                scrumMaster 'rprins'
            }
            
		    dropMergeOn every.even.monday //.includingToday
		
		    functionalDescription {
//		        withJiraIssuesTable "sprint = '${myProperties['sprintName']}' AND resolution = Fixed AND issuetype not in ('Bug during story', Todo)"
		        withJiraIssuesTable "component='CWS team' AND resolution = Fixed AND issuetype not in ('Bug during story', Todo) and status in (Delivered, Done) and resolutionDate>'${myProperties['wipStartDate']}' and resolutionDate<='${myProperties['wipEndDate']}'"
		    }

            wiki {
                userName myProperties['wikiUserName']
                password myProperties['wikiPassword']
                pageId myProperties['wikiDropMergePageId']
            }

            crucible {
                userName myProperties['crucibleUserName']
                password myProperties['cruciblePassword']
                projectKey 'CWS'
            }

            jenkins {
                regressionTests {
                    Jenkins globalUIUnits = new Jenkins('http://10.192.69.9:8080')
                    ofType('JUnit') {
                        withJob { job 'cws-wip-junit-l' on buildMasterNL; description 'Linux' }
                        comparedToJob { job 'CWS-L' on jenkinsOfSVT; description 'Linux' }
                        andJob { job 'CWSOldBuild-L' on jenkinsOfSVT; description 'Linux' }
                        andJob { job 'CAP-CWS-L' on jenkinsOfSVT; description 'Linux' }

                        withJob { job 'cws-wip-junit-w' on buildMasterNL; description 'Windows' }
                        comparedToJob { job 'CWS-W' on jenkinsOfSVT; description 'Windows' }
                        andJob { job 'CWSOldBuild-W' on jenkinsOfSVT; description 'Windows' }
                        andJob { job 'CAP-CWS-W' on jenkinsOfSVT; description 'Windows' }
                    }
                    ofType('UIUnits') {
                    
                        withJob {
                            job 'cws-wip-uiunit' on buildMasterNL matrixValues browser: 'Chrome';
                            description 'Chrome'
                        }
                        comparedToJob{ job 'CWS-Trunk' on globalUIUnits}
                        withJob {
                            on buildMasterNL job 'cws-wip-uiunit' matrixValues browser: 'Firefox';
                            description 'Firefox'
                        }
                        comparedToJob{ job 'CWS-Trunk' on globalUIUnits}
                     }
                    ofType('UIUnits Runtime Ref') {
                        withJob {
                           job 'cws-wip-uiunit-runtime-ref-test' on buildMasterNL  matrixValues browser: 'Chrome';
                            description 'Chrome'
                        }
                        comparedToJob{ job 'CWS-Runtime-Trunk' on globalUIUnits}
                        withJob {
                            job 'cws-wip-uiunit-runtime-ref-test' on buildMasterNL matrixValues browser: 'Firefox';
                            description 'Firefox'
                        }
                        comparedToJob{ job 'CWS-Runtime-Trunk' on globalUIUnits}
                    }
                    ofType('GMF') {
                        withJob { on buildMasterNL job 'cws-wip-gmf-chrome'; description 'Chrome' }
                        comparedToJob{job 'GraphicalModelingFramework-Trunk' on globalUIUnits }
                        withJob { on buildMasterNL job 'cws-wip-gmf-ff'; description 'Firefox' }
                        comparedToJob{job 'GraphicalModelingFramework-Trunk' on globalUIUnits }
                    }
                }
                upgrade {
                    withJob { on buildMasterNL job 'A_UP_4.1CU7.1-WIP_W'; description 'on Windows from BOP 4.1 CU7.1 to WIP' }
                    withJob { on buildMasterNL job 'B_UP_4.1CU6-4.1CU7.1_L'; description 'on Linux from BOP 4.1 CU6 to BOP 4.1 CU7.1' }
                    withJob { on buildMasterNL job 'C_UP_4.1CU7.1-WIP_L'; description 'on Linux from BOP 4.1 CU7.1 to WIP' }
                    withJob { on buildMasterNL job 'D_UP_4.2CU1-WIP_L'; description 'on Linux from BOP 4.2 CU1' }
                    withJob { on buildMasterNL job 'E_UP_4.1CU7.1-WIP_W'; description 'on Windows from BOP 4.1 CU7.1' }
                    withJob { on buildMasterNL job 'F_UP_4.2CU1-WIP_W'; description 'on Windows from BOP 4.2 CU1' }
                    withJob { on buildMasterNL job 'wip-content-upgrade'; description 'on Windows from BOP4.1 CU7.1 to WIP with design-time content' }
                }
                pmd {
		            trunk { job 'cws-wip-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
		        compilerWarnings {
		            trunk { job 'cws-wip-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
		        mbv {
		            trunk { job 'cws-wip-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
                integrationTests {
                    withJob { job 'cws-wip-uiunit-EW-NewBuildEngine' on buildMasterNL; description 'Eastwind' }
                    withJob { job 'cws-wip-smoketest' on buildMasterNL; description 'Smoke Test'}
                }
            }
        qualityAndProcessQuestions {
        	xmlMemoryManagementIssuesIntroduced no, 'INU detection enabled.'
        	regressionTestsPassWithPayloadValidation yes
        	compliantWithHorizontalComponentRequirements yes
        	documentationReviewed yes, 'Embedded in the development process.'
        	defectFixesRetestedByOtherPerson yes
        	alertsDocumented notApplicable
        	multiPlatformValidationDone yes
        	buildAndInstallerChangesAddressed notApplicable
        	messagesTranslatable yes, 'Embedded in the development process.'
        	newManualTestCasesAdded no, 'No new manual tests added. We prefer automated tests.'
        	forwardPortingCompleted yes, 'We always first fix in our own WIP.'
        	securityIssuesIntroduced no, 'Guarded by code reviews.'
        	migrationAspectsHandled yes
        	performanceDegraded no
					backwardCompatibilityIssuesIntroduced no
        	/*{
        		withHtml JenkinsSpec.getJenkinsUrlWithStatus(new Jenkins('http://buildmaster-nl/jenkins').withJob('FP1-LOADTEST-MYSQL'))
        	}*/
        }
        
     }
        

    }
}