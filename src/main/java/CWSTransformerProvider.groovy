import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.dsl.DropMergeInput

class CWSTransformerProvider {

    public static void main(String[] args) {
        DropMergeInput.provide {
            team {
                name 'CWS'
                productManager 'hkastenb'
                architect 'rprins'
                scrumMaster 'jhoegee'
                developmentManager 'gligtenb'
            }

		    dropMergeOn every.even.monday.includingToday

					functionalDescription {
//		        withJiraIssuesTable "sprint = '${myProperties['sprintName']}' AND resolution = Fixed AND issuetype not in ('Bug during story', Todo)"
		        withJiraIssuesTable "filter='EMNL Team' AND component != Provisioning AND resolution = Fixed AND issuetype not in (Escalation-Sub, 'QA Sub-task', Sub-task) and status in (Verified, Closed) and resolutionDate>'${myProperties['wipStartDate']}' and resolutionDate<='${myProperties['wipEndDate']}' ORDER BY status ASC"
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
                    Jenkins globalUIUnits = new Jenkins('http://cin9002.opentext.net:8080')
                    ofType('JUnit') {
                        withJob { job 'cws-wip-junit-l' on buildMasterNL; description 'Linux' }
                        comparedToJob { job 'CWS-L' on jenkinsOfSVT; description 'Linux' }
                    //    andJob { job 'CWSOldBuild-L' on jenkinsOfSVT; description 'Linux' }
                    //    andJob { job 'CAP-CWS-L' on jenkinsOfSVT; description 'Linux' }

                        withJob { job 'cws-wip-junit-w' on buildMasterNL; description 'Windows' }
                        comparedToJob { job 'CWS-W' on jenkinsOfSVT; description 'Windows' }
                    //    andJob { job 'CWSOldBuild-W' on jenkinsOfSVT; description 'Windows' }
                    //    andJob { job 'CAP-CWS-W' on jenkinsOfSVT; description 'Windows' }
						
						withJob { job 'cws-entity-test' on buildMasterNL; description 'Linux' }
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
                    //    comparedToJob{ job 'CWS-Trunk' on globalUIUnits}
                        withJob {
                            on buildMasterNL job 'cws-wip-uiunit-IE11' matrixValues browser: 'IE11-on-Windows7';
                            description 'IE11'
                        }
                    //    comparedToJob{ job 'CWS-Trunk' on globalUIUnits}
		
						withJob { job 'cws-entity-uiunit' on buildMasterNL matrixValues browser: 'Chrome'; description 'Chrome' }
						
						withJob { job 'cws-entity-uiunit' on buildMasterNL matrixValues browser: 'Firefox'; description 'Firefox' }

                        withJob { 
                            job 'cws-entity-uiunit-IE11' on buildMasterNL matrixValues browser: 'IE11-on-Windows7';
                            description 'IE11'
                        }
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
                    //    comparedToJob{ job 'CWS-Runtime-Trunk' on globalUIUnits}
                    }
                    ofType('GMF') {
                        withJob { on buildMasterNL job 'cws-wip-gmf-chrome'; description 'Chrome' }
                        comparedToJob{job 'GraphicalModelingFramework-Trunk' on globalUIUnits }
                        withJob { on buildMasterNL job 'cws-wip-gmf-ff'; description 'Firefox' }
                    //    comparedToJob{job 'GraphicalModelingFramework-Trunk' on globalUIUnits }
                        withJob { on buildMasterNL job 'cws-wip-gmf-ie11'; description 'IE11' }
                    }
                }
                upgrade {
					withJob { on buildMasterNL job 'UP_A_10.8_WIP_W_content'; description '(A) on Windows from PP 10.8 to WIP for cws-wip-upgrade-content' }
					withJob { on buildMasterNL job 'cws-wip-content-upgrade'; description 'on Windows from PP 10.8 to WIP with design-time content' }
					withJob { on buildMasterNL job 'UP_B_10.7_WIP_W';         description '(B) on Windows from PP 10.7 to WIP' }
					withJob { on buildMasterNL job 'UP_C_10.7_WIP_L';         description '(C) on Linux from PP 10.7 to WIP' }
					withJob { on buildMasterNL job 'UP_F_16_WIP_W';   	      description '(F) on Windows from PP 16 to WIP' }
					withJob { on buildMasterNL job 'UP_G_16_WIP_L';     	  description '(G) on Linux from PP 16 to WIP' }
					withJob { on buildMasterNL job 'UP_H_16.1_WIP_W';  		  description '(H) on Windows from PP 16.1 to WIP' }
					withJob { on buildMasterNL job 'UP_I_16.1_WIP_L';  		  description '(I) on Linux from PP 16.1 to WIP' }
					withJob { on buildMasterNL job 'UP_J_16.1.2_WIP_W';       description '(J) on Windows from PP 16.1.2 to WIP' }
					withJob { on buildMasterNL job 'UP_K_16.1.2_WIP_L';       description '(K) on Linux from PP 16.1.2 to WIP' }
                }
                pmd {
		            trunk { job 'cws-trunk-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
		        compilerWarnings {
		            trunk { job 'cws-trunk-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
		        mbv {
		            trunk { job 'cws-trunk-metrics' on buildMasterNL }
		            wip { job 'cws-wip-metrics' on buildMasterNL }
		        }
                integrationTests {
                    withJob { job 'cws-wip-uiunit-EW' on buildMasterNL; description 'Eastwind' }
                    withJob { job 'cws-wip-smoketest' on buildMasterNL; description 'Smoke Test'}
                }
                performanceDegraded {
                    withJob { job 'cws-performance-test-mysql' on buildMasterNL; description 'MySQL' }
                    withJob { job 'cws-performance-test-sqlserver' on buildMasterNL; description 'SQLServer' }
                    withJob { job 'cws-performance-test-oracle' on buildMasterNL; description 'Oracle' }
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
			backwardCompatibilityIssuesIntroduced no
			usabilityAcceptedByPM yes
			userStoriesAcceptedByPM yes
            buildModelersSucceeds yes
            fullDropMerge yes
        	/*{
        		withHtml JenkinsSpec.getJenkinsUrlWithStatus(new Jenkins('http://buildmaster-nl/jenkins').withJob('FP1-LOADTEST-MYSQL'))
        	}*/
        }
        
     }
        

    }
}