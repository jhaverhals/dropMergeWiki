package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.JenkinsJob
import org.junit.Ignore
import org.junit.Test


class ComparePMD {
    static final Jenkins jenkinsOfCMT = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')
    static final Jenkins buildMasterNL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    @Test
    @Ignore
    public void doTest() {
//        JenkinsJob pmdT = jenkinsOfCMT.withJob('PMD-Trunk-PCT')
        JenkinsJob pmdT = buildMasterNL.withJob('pct-trunk-build-installer-l-x64')
        JenkinsJob pmdW = buildMasterNL.withJob('pct-trunk-wip-build-installer-l-x64')

        Jenkins.getPMDDiffsPerSuite(pmdT, pmdW).entrySet().each {
            println it.key   + '\t' + it.value
        }

    }
}
