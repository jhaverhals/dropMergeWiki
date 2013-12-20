package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.TransformerProvider
import groovy.xml.MarkupBuilder

class FreeTextSpec {
    private StringBuilder sb = new StringBuilder()

    void withText(String text) {
        withHtml { MarkupBuilder html -> html.p(text) }
    }

    void withHtml(Closure text) {
        sb.append TransformerProvider.withHtml(text).call()
    }

    void withJiraIssuesTable(String query) {
        sb.append TransformerProvider.getJiraIssues(query)
    }

    public String getText() {
        return sb.toString()
    }
}
