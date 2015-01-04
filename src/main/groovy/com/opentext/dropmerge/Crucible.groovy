package com.opentext.dropmerge

import groovyx.net.http.HTTPBuilder

class Crucible {
    public static final String crucibleServer = 'http://codereview.vanenburg.com:8060/'

    private static URL getOpenReviewsRSSURL(String project, String authToken) {
        return new URL(crucibleServer + 'cru/rssReviewFilter?filter=allOpenReviews&project=' + project + '&FEAUTH=' + authToken)
    }

    public static String getBrowseReviewsURL(String project) {
        return crucibleServer + 'cru/browse/' + project
    }

    public static def getXMLForOpenReview(String project, String authToken) {
        return new XmlSlurper().parseText(getOpenReviewsRSSURL(project, authToken).text)
    }

    public static int getOpenReviewCount(String project, String authToken) {
        getXMLForOpenReview(project, authToken).channel.'*'.findAll { it ->
            it.name() == 'item' && !it.description.text().contains('NODROPMERGEBLOCKER')
        }.size()
    }

    public static String getCrucibleAuthToken(String username, String password) {
        String authToken = null
        new HTTPBuilder(crucibleServer).get(path: '/rest-service/auth-v1/login',
                query: [userName: username, password: password]) { resp, reader ->
            assert resp.statusLine.statusCode == 200
            authToken = reader.token.text()
            assert authToken != null
        }

        return authToken
    }

}
