package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.CordysWiki
import com.opentext.dropmerge.TransformerProvider


class TeamSpec {

    private Map<String, Closure<String>> inputs
    private Set<String> userNames = []

    TeamSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(userNames)
    }

    def name(String name) {
        inputs['TeamLink'] = { item -> CordysWiki.selectOption(item, "$name team") }
    }

    def scrumMaster(String... userName) {
        handleAddition('ScrumMasterName', userName)
    }

    def architect(String... userName) {
        handleAddition('ArchitectName', userName)
    }

    def productManager(String... userName) {
        handleAddition('ProductManagerName', userName)
    }

    private void handleAddition(String field, String... userNamesParam) {
        use(StringClosureCategories) {
            userNames.addAll userNamesParam
            userNamesParam.each {userName ->
                if (inputs.containsKey(field)) {
                    inputs[field] += { ', ' + TransformerProvider.getUserLink(userName) }
                } else {
                    inputs[field] = { (field == 'ProductManagerName' ? '' : ' ') + TransformerProvider.getUserLink(userName) }
                }
            }
        }
    }

    def otherMembers(String... otherUserNames) {
        userNames.addAll otherUserNames
    }
}
