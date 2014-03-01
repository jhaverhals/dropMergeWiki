package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.CordysWiki
import com.opentext.dropmerge.TransformerProvider


class TeamSpec {

    private Map<String, Closure<String>> inputs
    private Set<String> userNames = []

    TeamSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    def name(String name) {
        inputs['TeamLink'] = { item -> CordysWiki.selectOption(item, "$name team") }
    }


    def scrumMaster(String fullName, String userName) {
        handleAddition('ScrumMasterName', userName, fullName)
    }

    def architect(String fullName, String userName) {
        handleAddition('ArchitectName', userName, fullName)
    }

    def productManager(String fullName, String userName) {
        handleAddition('ProductManagerName', userName, fullName)
    }

    private void handleAddition(String field, String userName, String fullName) {
        use(StringClosureCategories) {
            userNames.add userName
            if (inputs.containsKey(field)) {
                inputs[field] += { ', ' + TransformerProvider.getUserLink(userName) }
            } else {
                inputs[field] = { (field != 'ProductManagerName' ? ' ' : '') + TransformerProvider.getUserLink(userName) }
            }
        }
    }

    def otherMembers(String... otherUserNames) {
        userNames << otherUserNames
    }
}
