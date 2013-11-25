package com.opentext.dropmerge.dsl;

class WikiSpec {
    String userName
    String password
    String pageId

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def pageId(String id) { this.pageId = id }

    def pageId(int id) { pageId("$id") }
}