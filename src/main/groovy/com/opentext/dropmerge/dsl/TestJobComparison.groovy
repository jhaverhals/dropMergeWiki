package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.WikiTableBuilder
import groovy.xml.MarkupBuilder

class TestJobComparison {
    WikiTableBuilder table;

    static TestJobComparison compare(@DelegatesTo(TestJobComparison) Closure closure) {
        Writer writer = new StringWriter()
        MarkupBuilder htmlBuilder = com.opentext.dropmerge.TransformerProvider.newMarkupBuilder(writer)

        TestJobComparison inputDsl = new TestJobComparison()
        inputDsl.table = new WikiTableBuilder(htmlBuilder)
        inputDsl.with closure
        inputDsl.table.process()

        new File('report.html').write writer.toString()

        return inputDsl
    }

    static final Jenkins jenkinsOfSVT = new Jenkins('http://srv-ind-svt9l.vanenburg.com:8080')

    static final Jenkins jenkinsOfCMT = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')

    static final Jenkins buildMasterHYD = new Jenkins('http://buildmaster-hyd.vanenburg.com/jenkins')

    static final Jenkins buildMasterNL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    def forTeam(String teamName, @DelegatesTo(ComparingJobsSpec) jobsClosure) {
        ComparingJobsSpec jobsSpec = new ComparingJobsSpec()
        jobsSpec.with jobsClosure

        jobsSpec.comparableJobSpecs.each { JobSpec wip, JobSpec trunk ->
            Jenkins.getTestDiffsPerSuite(trunk.jenkinsJob, wip.jenkinsJob).each { k, v ->
                table.addRow(
                        'Suite / Test': k,
                        'Difference': String.format('%+d', v),
                        'Type': teamName + (wip.description ? " - ${wip.description}" : ''),
                        'Justification': jobsSpec.justifications[wip]?.getJustificationsForClassName(k)
                )
            }
        }
    }
}
