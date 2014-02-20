package com.opentext.dropmerge.dsl

import org.junit.Test

import java.text.SimpleDateFormat

//@TypeChecked
public class DslTest {

    @Test
    public void testTeam() {
        Map<String, Closure<String>> inputs = DropMergeInput.provide {
            team { name 'Platform Core' }
            skipPersist
        }.inputs

        assert inputs.containsKey('TeamLink')
        assert !inputs.containsKey('DropMergeDate')
    }

    @Test
    public void testMuchMore() {
        Map<String, Closure<String>> date = DropMergeInput.provide {
            team {
                name 'Platform Core'
                scrumMaster 'Gerwin Jansen', 'gjansen'
                architect 'Willem Jan Gerritsen', 'wjgerrit'
                productManager 'Johan Pluimers', 'jpluimer'
            }

            dropMergeOn every.odd.friday.includingToday

            jenkins {
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
                    withJob { job 'pct-upgrade-trigger-l' on buildMasterNL; description 'from latest GA (BOP 4.3) to latest wip.' }
                    withJob { job 'pct-upgrade-trigger-w' on buildMasterNL; description 'from BOP 4.1 CU7.1 to latest wip.' }
                }
            }

            skipPersist
        }.inputs

        assert date.containsKey('TeamLink')
        assert date.containsKey('DropMergeDate')

        Calendar c = Calendar.getInstance()
        while (c.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY)
            c.add(Calendar.DAY_OF_WEEK, 1)
        if (c.get(Calendar.WEEK_OF_YEAR) % 2 == 0)
            c.add(Calendar.WEEK_OF_YEAR, 1)


        assert date['DropMergeDate'].call() == new SimpleDateFormat("yyyy-MM-dd 13:00:00").format(c.time)
        assert ((String) date['ScrumMasterName'].call()).contains('gjansen')
    }
}