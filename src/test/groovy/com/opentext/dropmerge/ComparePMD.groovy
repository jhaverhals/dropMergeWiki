package com.opentext.dropmerge

import groovy.json.JsonSlurper
import spock.lang.Specification


class ComparePMD extends Specification {
    JenkinsJob mockJobWithPMDReport(String report) {
        JenkinsJob job = Mock()
        job.getPMDReport() >> new JsonSlurper().parse(this.getClass().getResource(report).newReader())
        return job
    }

    def "casesPerSuite: identicalPaths_identicalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithPMDReport('pmdResults1.json')
        JenkinsJob jobB = mockJobWithPMDReport('pmdResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.PMDReport)
        def casesB = Jenkins.casesPerSuite(jobB.PMDReport)

        then:
        casesA.size() > 0
        casesA.size() == casesB.size()
        casesA == casesB
    }

    def "correlateKeys: identicalPaths"() {

        setup:
        JenkinsJob jobA = mockJobWithPMDReport('pmdResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.PMDReport).keySet()
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
        JenkinsJob jobA = mockJobWithPMDReport('pmdResults1.json')
        JenkinsJob jobB = mockJobWithPMDReport('pmdResults1.json')

        when:
        def casesA = Jenkins.casesPerSuite(jobA.PMDReport)
        def casesB = Jenkins.casesPerSuite(jobB.PMDReport)
        def keyMap = Jenkins.correlateKeys(casesA.keySet(), casesB.keySet())

        then:
        keyMap.size() > 0
        keyMap.size() == casesA.size()
        keyMap.size() == casesB.size()
        keyMap.every { String a, String b ->
            casesA.containsKey(a) && casesB.containsKey(b) && casesA[a].equals(casesB[b])
        }
        Jenkins.getPMDDiffsPerSuite(jobA, jobB).size() == 0
    }

    def "identicalPaths_nonIdenticalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithPMDReport('pmdResults1.json')
        JenkinsJob jobB = mockJobWithPMDReport('pmdResults2.json')

        when:
        def pmdDiffsPerSuite = Jenkins.getPMDDiffsPerSuite(jobA, jobB)

        then:
        pmdDiffsPerSuite.size() == 6
        pmdDiffsPerSuite == ["/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/basicutil/src/java/com/eibus/xml/dom/DocumentWriter.java": -1,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/esbclient/src/java/com/eibus/directory/soap/Cache.java": 5,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/esbclient/src/java/com/eibus/directory/soap/LDAPUtil.java": 2,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/esbclient/src/java/com/eibus/role/UserRolesCache.java": 1,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/esbclient/src/java/com/eibus/role/RoleCache.java": 1,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/secadmin/src/java/com/cordys/persistence/xds/XDSServiceFactory.java": -1]
    }

    def "nonidenticalPaths_nonIdenticalViolations"() {

        setup:
        JenkinsJob jobA = mockJobWithPMDReport('pmdResults1.json')
        JenkinsJob jobB = mockJobWithPMDReport('pmdResults3.json')

        when:
        def pmdDiffsPerSuite = Jenkins.getPMDDiffsPerSuite(jobA, jobB)

        then:
        pmdDiffsPerSuite.size() == 6
        pmdDiffsPerSuite == ["/usr1/workspace/PlatformCore-pmd/components/basicutil/src/java/com/eibus/xml/dom/DocumentWriter.java": -1,
                "/usr1/workspace/PlatformCore-pmd/components/esbclient/src/java/com/eibus/directory/soap/Cache.java": 5,
                "/usr1/workspace/PlatformCore-pmd/components/esbclient/src/java/com/eibus/directory/soap/LDAPUtil.java": 2,
                "/usr1/workspace/PlatformCore-pmd/components/esbclient/src/java/com/eibus/role/UserRolesCache.java": 1,
                "/usr1/workspace/PlatformCore-pmd/components/esbclient/src/java/com/eibus/role/RoleCache.java": 1,
                "/opt/jenkins/workspace/pct-trunk-wip-build-installer-l-x64/components/secadmin/src/java/com/cordys/persistence/xds/XDSServiceFactory.java": -1]
    }
}
