package com.opentext.dropmerge.dsl

import static com.opentext.dropmerge.CordysWiki.selectOption
import static com.opentext.dropmerge.Crucible.*
import static com.opentext.dropmerge.TransformerProvider.getLink

class CrucibleSpec extends Spec {
    String userName
    String password
    String projectKey

    def userName(String userName) { this.userName = userName }

    def password(String password) { this.password = password }

    def projectKey(String key) { this.projectKey = key }

    @Override
    Map<String, Closure<String>> getInputData() {
        Map<String, Closure<String>> inputs = new HashMap<>()

        final String crucibleAuthToken = getCrucibleAuthToken(this.userName, this.password)
        final int openReviewCount = getOpenReviewCount(this.projectKey, crucibleAuthToken)

        inputs['ReviewsDone'] = { item ->
            selectOption item, openReviewCount == 0 ? 'Yes' : 'No'
        }
        inputs['ReviewsDoneComment'] = {
            getLink getBrowseReviewsURL(this.projectKey),
                    openReviewCount == 0 ? 'All reviews closed' : "$openReviewCount open review(s)"
        }

        return inputs
    }
}