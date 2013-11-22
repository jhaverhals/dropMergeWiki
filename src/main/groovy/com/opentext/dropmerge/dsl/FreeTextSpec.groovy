package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.TransformerProvider
import groovy.xml.MarkupBuilder


class FreeTextSpec {
    StringBuilder sb = new StringBuilder()

    def withText(String text) {
        withHtml { MarkupBuilder html -> html.p(text) }
    }

    def withHtml(Closure text) {
        sb.append TransformerProvider.withHtml(text).call()
    }

    def withJiraIssuesTable(String query) {
       sb.append TransformerProvider.getJiraIssues(query)
    }
}
