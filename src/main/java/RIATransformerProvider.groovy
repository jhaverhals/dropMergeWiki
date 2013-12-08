import com.opentext.dropmerge.dsl.DropMergeInput

public class RIATransformerProvider {

    public static void main(String[] args) {
        DropMergeInput.provide {
            team {
                name 'RIA'
            }

            jenkins {
                mbv {
                    wip { job 'MBV-RIA' on buildMasterHYD }
                    trunk { job 'MBV-RIA' on jenkinsOfCMT }
                }
                pmd {
                    wip { job 'RIA-FULLREPORT' on buildMasterHYD }
                    trunk { job 'RIA' on jenkinsOfCMT }
                }
            }
        }
    }
}