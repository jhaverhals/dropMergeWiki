package com.opentext.dropmerge.dsl

import java.text.SimpleDateFormat

import com.opentext.dropmerge.CordysWiki
import com.opentext.dropmerge.Crucible
import com.opentext.dropmerge.TransformerProvider
import com.opentext.dropmerge.UpdateWikiProperties

public class DropMergeInput {
    public Map<String, Closure<String>> inputs = new HashMap<String, Closure<String>>()
    public static final UpdateWikiProperties myProperties = loadProperties('team.properties', 'user.properties', 'session.properties')
    private WikiSpec wikiSpecification
    private TeamSpec teamSpecification
    private boolean persist = true


    static DropMergeInput provide(@DelegatesTo(DropMergeInput) Closure closure) {
        DropMergeInput inputDsl = new DropMergeInput()
        inputDsl.with closure

        inputDsl.persist()

        return inputDsl
    }

    private void persist() {
        if (persist) {
            new CordysWiki().with {
                authenticate(wikiSpecification.userName, wikiSpecification.password)
                updateDropMergePage(wikiSpecification.pageId, inputs, true)
            }
        }
    }

    def getSkipPersist() {
        persist = false
    }

    static UpdateWikiProperties loadProperties(String... files) {
        def p = new Properties()
        files.each { String it ->
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

    def team(@DelegatesTo(TeamSpec) Closure team) {
        if(!teamSpecification) {
            teamSpecification = new TeamSpec(inputs)
        }
        teamSpecification.with team
    }

    TeamSpec getTeam() {
        return teamSpecification
    }

    DateDsl getEvery() { new DateDsl() }

    DateDsl getNext() { new DateDsl().setIncludeToday(false) }

    def dropMergeOn(DateDsl date) {
        dropMergeOn(date.getDate())
    }

    def dropMergeOn(Date date) {
        inputs['DropMergeDate'] = { new SimpleDateFormat("yyyy-MM-dd 13:00:00").format(date) }
    }

    def goToCCB(DateDsl date) {
        goToCCB(date.getDate())
    }

    def goToCCB(Date date) {
        inputs['CCBDate'] = { new SimpleDateFormat("yyyy-MM-dd 13:00:00").format(date) }
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
            TransformerProvider.getLink(Crucible.getBrowseReviewsURL(crucibleSpec.projectKey),
                    (openReviewCount > 0 ? "$openReviewCount open review(s)" : 'All reviews closed')
            )
        }
    }

    def wiki(@DelegatesTo(WikiSpec) Closure wiki) {
        WikiSpec wikiSpec = new WikiSpec()
        wikiSpec.with wiki

        this.wikiSpecification = wikiSpec;
    }

    def jenkins(@DelegatesTo(JenkinsSpec) Closure jenkins) {
        JenkinsSpec jenkinsSpec = new JenkinsSpec(inputs)
        jenkinsSpec.with jenkins
    }

    def qualityAndProcessQuestions(@DelegatesTo(QualityAndProcessQuestionsSpec) Closure jenkins) {
        QualityAndProcessQuestionsSpec questionsSpec = new QualityAndProcessQuestionsSpec(inputs)
        questionsSpec.with jenkins
    }

    def functionalDescription(@DelegatesTo(FreeTextSpec) Closure desc) {
        FreeTextSpec freeTextSpec = new FreeTextSpec()
        freeTextSpec.with desc

        inputs['FunctionalDescription'] = { freeTextSpec.getText() }
    }

}
