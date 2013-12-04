package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.CordysWiki
import com.opentext.dropmerge.TransformerProvider


class TeamSpec {

    private Map<String, Closure<String>> inputs
    private List<String> userNames = []

    TeamSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    def name(String name) {
        inputs['Team'] = { item -> CordysWiki.selectOption(item, name) }
    }


    def scrumMaster(String fullName, String userName) {
        userNames.add(userName)
        inputs['ScrumMasterName'] = { ' ' + TransformerProvider.getUserLink(userName, fullName) }
    }

    def architect(String fullName, String userName) {
        userNames.add(userName)
        inputs['ArchitectName'] = { ' ' + TransformerProvider.getUserLink(userName, fullName) }
    }

    def productManager(String fullName, String userName) {
        userNames.add(userName)
        inputs['ProductManagerName'] = { TransformerProvider.getUserLink(userName, fullName) }
    }

    def otherMembers(String... otherUserNames) {
        userNames << otherUserNames
    }
}
