package com.opentext.dropmerge

import com.opentext.testutils.junit.HttpServerRule
import groovy.json.JsonBuilder
import org.junit.Rule
import spock.lang.Specification

import static com.opentext.dropmerge.TestCount.*

class JenkinsJobTest extends Specification {
    @Rule
    HttpServerRule jenkinsServer = new HttpServerRule()

    final static String JOB_NAME = 'abc'

    void addJSON(String url, Closure jsonC) {
        JsonBuilder b = new JsonBuilder()
        b.with { json -> json jsonC }
        jenkinsServer.addJSONResponseForPath(url, b)
    }

    def 'last build result'() {
        setup:
        Jenkins jenkinsInstance = new Jenkins(jenkinsServer.URI)

        when:
        [lastCompletedBuild: 'SUCCESS', lastBuild: 'RUNNING'].each { name, resultValue ->
            addJSON("job/$JOB_NAME/$name/api/json?tree=result") { result resultValue }
        }

        then:
        jenkinsInstance.withJob(JOB_NAME).lastBuildResult == 'SUCCESS'
    }

    def 'matrix sub jobs'() {
        setup:
        Jenkins jenkinsInstance = new Jenkins(jenkinsServer.URI)

        when:
        addJSON("job/$JOB_NAME/api/json?tree=activeConfigurations[name]") {
            activeConfigurations([{ name 'component=a' }, { name 'component=b' }, { name 'component=c' }])
        }

        and:
        Map colors = ['component=a': 'blue', 'component=b': 'aborted', 'component=c': 'yellow_anime']
        colors.each { name, resultValue ->
            addJSON("job/$JOB_NAME/$name/api/json?tree=color") { color resultValue }
            addJSON("job/$JOB_NAME/$name/api/json?tree=activeConfigurations[name]") {}
        }

        then:
        jenkinsInstance.withJob(JOB_NAME).matrixSubJobs*.color == colors*.value
        jenkinsInstance.withJob(JOB_NAME, [component: 'a']).matrixSubJobs == []
        jenkinsInstance.withJob(JOB_NAME + '/component=a').matrixSubJobs == []
    }

    def 'test figure - matrix sub jobs'() {
        setup:
        Jenkins jenkinsInstance = new Jenkins(jenkinsServer.URI)

        when:
        addJSON("job/$JOB_NAME/api/json?tree=activeConfigurations[name]") {
            activeConfigurations([{ name 'component=a' }, { name 'component=b' }, { name 'component=c' }])
        }

        and:
        Map colors = [
                'component=a': [failCount: 0, passCount: 1, skipCount: 2],
                'component=b': [failCount: 1, passCount: 3, skipCount: 0],
                'component=c': [failCount: 1, passCount: 4, skipCount: 1]]
        colors.each { name, resultValue ->
            addJSON("job/$JOB_NAME/$name/lastSuccessfulBuild/testReport/api/json?tree=passCount,failCount,totalCount,skipCount") {
                failCount resultValue['failCount']
                passCount resultValue['passCount']
                skipCount resultValue['skipCount']
            }

            addJSON("job/$JOB_NAME/$name/api/json?tree=activeConfigurations[name]") {}
        }

        then:
        jenkinsInstance.withJob(JOB_NAME).getTestFigure(Pass) == '8'
        jenkinsInstance.withJob(JOB_NAME).getTestFigure(Fail) == '2'
        jenkinsInstance.withJob(JOB_NAME).getTestFigure(Skip) == '3'
        [Fail, Pass, Skip].collect {
            jenkinsInstance.withJob(JOB_NAME, [component: 'a']).getTestFigure(it)
        } == ['0', '1', '2']

        jenkinsServer.invocationCount == 5
    }

    def 'color'() {
        setup:
        Jenkins jenkinsInstance = new Jenkins(jenkinsServer.URI)

        when:
        addJSON("job/$JOB_NAME/api/json?tree=color") { color 'blue' }

        then:
        jenkinsInstance.withJob(JOB_NAME).color == 'blue'
    }

}
