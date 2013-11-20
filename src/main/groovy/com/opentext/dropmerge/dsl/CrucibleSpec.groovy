package com.opentext.dropmerge.dsl;

class CrucibleSpec {
    String userName
    String password
    String projectKey

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def projectKey(String key) { this.projectKey = key }
}