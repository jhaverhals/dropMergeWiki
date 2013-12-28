package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.Jenkins
import com.opentext.dropmerge.WikiTableBuilder
import groovy.xml.MarkupBuilder

class TestJobComparison {
    WikiTableBuilder table;
    Properties props = loadProperties()

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

    def forTeam(String teamName, @DelegatesTo(ComparingJobsSpec) jobsClosure) {
        if (props.compareTeam && props.compareTeam != teamName)
            return

        ComparingJobsSpec jobsSpec = new ComparingJobsSpec()
        jobsSpec.with jobsClosure

        jobsSpec.comparableJobSpecs.each { JobSpec wip, JobSpec trunk ->
            Jenkins.getTestDiffsPerSuite(
                    jobsSpec.getJobSpecPlusLinkedJobSpecs(trunk).collect { it.jenkinsJob },
                    jobsSpec.getJobSpecPlusLinkedJobSpecs(wip).collect { it.jenkinsJob }
            ).each { k, v ->
                table.addRow(
                        'Suite / Test': k,
                        'Difference': String.format('%+d', v),
                        'Type': teamName + (wip.description ? " - ${wip.description}" : ''),
                        'Justification': jobsSpec.justifications[wip]?.getJustificationsForClassName(k)
                )
            }
        }
    }

    private Properties loadProperties() {
        def p = new Properties()
        def propertiesFromFile = new Properties()
        ['team.properties', 'user.properties', 'session.properties'].collect { new File(it) }.findAll { it.exists() }
                .each {
            propertiesFromFile.load(it.newInputStream())
        }

        final String propPrefix = this.class.simpleName
        (System.getenv() + propertiesFromFile + System.getProperties()).each { props ->
            props.each { prop ->
                ['.', '_'].each { sep ->
                    if (prop.key.startsWith(propPrefix + sep))
                        p[prop.key[propPrefix.length() + sep.length()..-1]] = prop.value
                }
            }
        }
        return p
    }
}
