package com.opentext.dropmerge.dsl

abstract class AnswerCommentPairSpec {
    protected String comment

    def comment(String text) { this.comment = text }
}
