import groovy.transform.TypeChecked

import com.opentext.dropmerge.dsl.DropMergeInput

@TypeChecked
class CWSTransformerProvider {

	public static void main(String[] args) {
		DropMergeInput.provide {
            team {
                name 'CWS'
                productManager 'Harmen Kastenberg', 'hkastenb'
                architect 'Rene Prins', 'rprins'
                scrumMaster 'Rene Prins', 'rprins'
            }

			jenkins {
				regressionTests {
					ofType('JUnit') {
						withJob { on buildMasterNL job 'cws-wip-junit-l'; description 'Linux' }
						withJob { on buildMasterNL job 'cws-wip-junit-w'; description 'Windows' }
					}
					ofType('GMF') {
						withJob { on buildMasterNL job 'cws-wip-gmf-chrome'; description 'Chrome' }
						withJob { on buildMasterNL job 'cws-wip-gmf-ff'; description 'Firefox' }
						withJob { on buildMasterNL job 'cws-wip-gmf-safari'; description 'Safari' }
					}
				}
			}
		}
	}
}