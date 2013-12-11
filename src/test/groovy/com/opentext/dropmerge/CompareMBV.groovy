package com.opentext.dropmerge

import groovy.json.JsonSlurper
import spock.lang.Specification


class CompareMBV extends Specification {
    JenkinsJob mockJobWithMBVReport(String report) {
        JenkinsJob job = Mock()
        job.getMBVReport() >> new JsonSlurper().parse(this.getClass().getResource(report).newReader())
        return job
    }

    def "casesPerSuite: identicalPaths_identicalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithMBVReport('mbvResults1.json')
        JenkinsJob jobB = mockJobWithMBVReport('mbvResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.MBVReport)
        def casesB = Jenkins.casesPerSuite(jobB.MBVReport)

        then:
        casesA.size() > 0
        casesA.size() == casesB.size()
        casesA == casesB
    }

    def "correlateKeys: identicalPaths"() {

        setup:
        JenkinsJob jobA = mockJobWithMBVReport('mbvResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.MBVReport).keySet()
        def keyMap = Jenkins.correlateKeys(casesA, casesA)

        then:
        keyMap.size() > 0
        keyMap.size() == casesA.size()
        casesA.every { String it ->
            keyMap.containsKey(it) && keyMap[it].equals(it)
        }
    }

    def "identicalPaths_identicalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithMBVReport('mbvResults1.json')
        JenkinsJob jobB = mockJobWithMBVReport('mbvResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.MBVReport)
        def casesB = Jenkins.casesPerSuite(jobB.MBVReport)
        def keyMap = Jenkins.correlateKeys(casesA.keySet(), casesB.keySet())

        then:
        keyMap.size() > 0
        keyMap.size() == casesA.size()
        keyMap.size() == casesB.size()
        keyMap.every { String a, String b ->
            casesA.containsKey(a) && casesB.containsKey(b) && casesA[a].equals(casesB[b])
        }
        Jenkins.getMBVDiffsPerSuite(jobA, jobB).size() == 0
    }

    def "identicalPaths_nonIdenticalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithMBVReport('mbvResults1.json')
        JenkinsJob jobB = mockJobWithMBVReport('mbvResults2.json')

        when:
        def pmdDiffsPerSuite = Jenkins.getMBVDiffsPerSuite(jobA, jobB)

        then:
        pmdDiffsPerSuite.size() == 2
        pmdDiffsPerSuite == ["/opt/jenkins/workspace/pct-trunk-wip-llc-l/components/cws/modelers/Common Content Runtime/commoncontent_DT/xforms/int-adminui/methodsetsmanager/methodslist#cws-xform#.cws": -3,
                "/opt/jenkins/workspace/pct-trunk-wip-llc-l/components/cws/modelers/Common Content Runtime/commoncontent_DT/xforms/int-adminui/sysresourcemgr/attachwebserviceinterfaces#cws-xform#.cws": 1]
    }

    def "nonidenticalPaths_nonIdenticalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithMBVReport('mbvResults1.json')
        JenkinsJob jobB = mockJobWithMBVReport('mbvResults3.json')

        when:
        def pmdDiffsPerSuite = Jenkins.getMBVDiffsPerSuite(jobA, jobB)

        then:
        pmdDiffsPerSuite.size() == 3
        pmdDiffsPerSuite == ["/usr1/Workspace/PlatformCore-mbv/components/cws/modelers/Common Content Runtime/commoncontent_DT/xforms/int-adminui/methodsetsmanager/methodslist#cws-xform#.cws": -3,
                "/usr1/Workspace/PlatformCore-mbv/components/cws/modelers/Common Content Runtime/commoncontent_DT/xforms/int-adminui/sysresourcemgr/attachwebserviceinterfaces#cws-xform#.cws": 1,
                "/opt/jenkins/workspace/pct-trunk-wip-llc-l/components/cws/modelers/Common Content Runtime/commoncontent_DT/xforms/int-adminui/sysresourcemgr/logger#cws-xform#.cws":-2]
    }
}
