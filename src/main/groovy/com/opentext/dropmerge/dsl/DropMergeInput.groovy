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
    private boolean persist = true
    private List<Spec> specs = new ArrayList<>()


    static DropMergeInput provide(@DelegatesTo(DropMergeInput) Closure closure) {
        // Define data
        DropMergeInput inputDsl = new DropMergeInput()
        inputDsl.with closure

        // Fetch data
        inputDsl.specs.each { inputs << it.inputData }

        // Persist to wiki
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
        TeamSpec teamSpec = new TeamSpec()
        teamSpec.with team

        specs << teamSpec
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

        specs << crucibleSpec
    }

    def wiki(@DelegatesTo(WikiSpec) Closure wiki) {
        WikiSpec wikiSpec = new WikiSpec()
        wikiSpec.with wiki

        this.wikiSpecification = wikiSpec;
    }

    def jenkins(@DelegatesTo(JenkinsSpec) Closure jenkins) {
        JenkinsSpec jenkinsSpec = new JenkinsSpec()
        jenkinsSpec.with jenkins

        specs << jenkinsSpec
    }

    def qualityAndProcessQuestions(@DelegatesTo(QualityAndProcessQuestionsSpec) Closure jenkins) {
        QualityAndProcessQuestionsSpec questionsSpec = new QualityAndProcessQuestionsSpec()
        questionsSpec.with jenkins

        specs << questionsSpec
    }

    def functionalDescription(@DelegatesTo(FreeTextSpec) Closure desc) {
        FreeTextSpec freeTextSpec = new FreeTextSpec()
        freeTextSpec.with desc

        inputs['FunctionalDescription'] = { freeTextSpec.getText() }
    }

}
