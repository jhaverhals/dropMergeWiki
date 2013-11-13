package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.*

import java.text.SimpleDateFormat

class DropMergeInput {
    public Map<String, Closure<String>> inputs = new HashMap<String, Closure<String>>()
    public static final UpdateWikiProperties myProperties = loadProperties('team.properties', 'user.properties', 'session.properties')


    static DropMergeInput provide(@DelegatesTo(DropMergeInput) Closure closure) {
        DropMergeInput inputDsl = new DropMergeInput()
        inputDsl.with closure

        return inputDsl
    }

    static UpdateWikiProperties loadProperties(String... files) {
        def p = new Properties()
        files.each {
            File f1 = new File(it)
            if (f1.exists()) p.load(f1.newInputStream())
        }

        final String propPrefix = 'updateWiki'
        [System.getenv(), System.getProperties()].each { props ->
            props.each { prop ->
                ['.', '_'].each { sep ->
                    if (prop.key.startsWith(propPrefix + sep)) p[prop.key[propPrefix.length() + 1..-1]] = prop.value
                }
            }
        }

        return new UpdateWikiProperties(p)
    }

    def team(String name) {
        inputs['Team'] = { item -> CordysWiki.selectOption(item, name) }
    }

    DateDsl getToday() { new DateDsl(); }

    DateDsl getNext() { new DateDsl().setIncludeToday(false); }

    DateDsl getNextEven() { new DateDsl().orNextEven; }

    DateDsl getNextOdd() { new DateDsl().orNextOdd; }

    def dropMergeOn(DateDsl date) {
        inputs['DropMergeDate'] = { new SimpleDateFormat("yyyy-MM-dd 13:00:00").format(date.getDate()); }
    }

    def scrumMaster(String fullName, String userName) {
        inputs['ScrumMaster'] = { ' ' + TransformerProvider.getUserLink(userName, fullName) }
    }

    def architect(String fullName, String userName) {
        inputs['ArchitectName'] = { ' ' + TransformerProvider.getUserLink(userName, fullName) }
    }

    def productManager(String fullName, String userName) {
        inputs['ProductManagerName'] = { TransformerProvider.getUserLink(userName, fullName) }
    }

    def crucible(@DelegatesTo(CrucibleSpec) Closure crucible) {
        CrucibleSpec crucibleSpec = new CrucibleSpec()
        crucibleSpec.with crucible

        final String crucibleAuthToken = Crucible.getCrucibleAuthToken(crucibleSpec.userName, crucibleSpec.password)
        final int openReviewCount = Crucible.getOpenReviewCount(crucibleSpec.projectKey, crucibleAuthToken)

        inputs['ReviewsDone'] = { item ->
            return CordysWiki.selectOption(item, (openReviewCount == 0 ? 'Yes' : 'No'))
        }
        inputs['ReviewsDoneComment'] = {
            getLink(Crucible.getBrowseReviewsURL(crucibleSpec.projectKey),
                    (openReviewCount > 0 ? "$openReviewCount open review(s)" : 'All reviews closed')
            )
        }
    }

    static final Jenkins jenkinsOfSVT = new Jenkins('http://srv-ind-svt9l.vanenburg.com:8080')

    static final Jenkins jenkinsOfCMT = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')

    static final Jenkins buildMasterHYD = new Jenkins('http://buildmaster-HYD.vanenburg.com/jenkins')

    static final Jenkins buildMasterNL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    def pmd(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['PMDViolationsHighBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumBefore'] = { jobSpec.trunk.getPMDFigure(WarningLevel.Normal) }
        inputs['PMDViolationsHighAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.High) }
        inputs['PMDViolationsMediumAfter'] = { jobSpec.wip.getPMDFigure(WarningLevel.Normal) }
    }

    def compilerWarnings(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['CompilerWarningsBefore'] = { jobSpec.trunk.compilerWarningFigure }
        inputs['CompilerWarningsAfter'] = { jobSpec.wip.compilerWarningFigure }
    }

    def mbv(@DelegatesTo(ComparableJobsSpec) jobs) {
        ComparableJobsSpec jobSpec = new ComparableJobsSpec()
        jobSpec.with jobs

        inputs['MBViolationsHighBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumBefore'] = { jobSpec.trunk.getMBFigure(WarningLevel.Normal) }
        inputs['MBViolationsHighAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.High) }
        inputs['MBViolationsMediumAfter'] = { jobSpec.wip.getMBFigure(WarningLevel.Normal) }
    }

    def upgrade(@DelegatesTo(JobsSpec) jobsClosure) {
        JobsSpec jobsSpec = new JobsSpec()
        jobsSpec.with jobsClosure;

        List<JobSpec> jobs = jobsSpec.jobs
        inputs['UpgradeTested'] = { item ->
            CordysWiki.selectOption(item, CordysWiki.selectOption(item, (jobs.every { JobSpec j -> j.jenkinsJob.lastBuildResult == 'SUCCESS' } == 0 ? 'Yes' : 'No')))
        }
        inputs['UpgradeTestedComment'] = TransformerProvider.withHtml { html ->
            html.p {
                jobs.each { JobSpec j ->
                    html.a(href: j.jenkinsJob.getBuildUrl(JenkinsJob.LAST_COMPLETED_BUILD), 'Upgrade job')
                    if (j.description) {
                        html.mkp.yield ' ' + j.description
                    }
                    html.br()
                }
            }
        }
    }
}
