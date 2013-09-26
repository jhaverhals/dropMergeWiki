import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import org.apache.http.impl.cookie.BasicClientCookie

import static groovyx.net.http.ContentType.URLENC

class CordysWiki {
    private HTTPBuilder wikiHttp = new HTTPBuilder('https://wiki.cordys.com')

    public void authenticate(String wikiUserName, String wikiPassword) {
        String tokenKey = null
        new HTTPBuilder('https://auth.cordys.com').post(path: '/sso/validate.do',
                contentType: URLENC,
                query: [userName: wikiUserName, password: wikiPassword, serviceUrl: 'www.cordys.com', Submit: '']) { resp, reader ->
            assert resp.statusLine.statusCode == 302
            tokenKey = resp.headers.find { h -> return h.name.equals('Set-Cookie') && h.value.startsWith('auth.token_key=') }.value
            assert tokenKey != null
        }

        String[] kvp = tokenKey.split('; ')[0].split('=')
        def authCookie = new BasicClientCookie(kvp[0], kvp[1])
        authCookie.domain = '.cordys.com'
        authCookie.path = '/'
        authCookie.secure = true

        wikiHttp.client.cookieStore.addCookie(authCookie)
    }

    public void eachDropMergeField(String pageID, Closure<?> closure) {
        wikiHttp.get(path: '/pages/editscaffold.action', query: [pageId: pageID]) { resp, reader ->
            getEditForm(reader).'**'
                    .findAll { isFormField(it) }
                    .collect { new FormField(it) }
                    .each(closure)
        }
    }

    public void updateDropMergePage(String pageID, Map<String, Closure<String>> transformers, boolean postToRealServer) {
        def updateQuery = [
                pageId: pageID,
                entityId: pageID,
                mode: 'edit',
                originalContent: '',
                wysiwygContent: '',
                decorator: 'none',
                formMode: 'forms',
                contentType: 'page',
                formName: 'scaffold-form',
                versionComment: 'Automatically updated by script.',
                notifyWatchers: 'false']


        wikiHttp.get(path: '/pages/editscaffold.action', query: [pageId: pageID]) { resp, reader ->
            def form = getEditForm(reader)

            ['originalVersion', 'conflictingVersion', 'parentPageString', 'newSpaceKey'].each {
                updateQuery[it] = getValueOfInputFieldById(form, it)
            }
            ['title': 'content-title', 'spaceKey': 'newSpaceKey'].each { targetParam, sourceFieldName ->
                updateQuery[targetParam] = getValueOfInputFieldById(form, sourceFieldName)
            }


            def json = new JsonBuilder()

            json {
                form.'**'
                        .findAll { isFormField(it) }
                        .collect { new FormField(it) }
                        .each { FormField formField ->

                    String contentValue = transform(transformers[formField.name], formField.rawItem, formField.content) ?: formField.content
                    "${formField.name}" {
                        name(formField.name)
                        children {}
                        parent(formField.parent)
                        type(formField.type)
                        params(formField.params)
                        content(contentValue)
                    }
                }
            }

            updateQuery['jsonContent'] = json.toString()
        }

        def destinationHttp = wikiHttp
        if (!postToRealServer) {
            destinationHttp = new HTTPBuilder('http://localhost')
            destinationHttp.setProxy('localhost', 8888, 'http')
        }

        destinationHttp.post(path: '/pages/doeditscaffold.action', contentType: URLENC, body: updateQuery) { resp ->
            println "HTTP response status: ${resp.statusLine}"
        }
    }

    public class FormField {
        def item

        private FormField(def item) {
            this.item = item
        }

        public def getRawItem() { item }

        public String getName() { item['@sd-name'].text() }

        public String getParent() { item['@sd-parent'].text() }

        public String getType() { item['@sd-type'].text() }

        public String getParams() { item['@sd-params'].text().trim() }

        public String getContent() { getAndFormatCurrentContent(item, type) }


    }

    private static boolean isFormField(def it) {
        it.name().equalsIgnoreCase('span') && it['@sd-name'] && it['@sd-name'].text()
    }

    private static getEditForm(def reader) {
        findByTagAndId(reader, 'form', 'editpageform')
    }

    private static def findByTagAndId(def node, String type, String id) {
        node.'**'.find { it.@id == id && it.name().equalsIgnoreCase(type) }
    }

    private static def getValueOfInputFieldById(def node, String id) {
        findByTagAndId(node, 'input', id).@value
    }

    public static def getJsonForOptions(item) {
        return new JsonSlurper().parseText(new String(item['@sd-options'].text().trim().decodeBase64()))
    }

    public static String selectOption(def item, String option) {
        def jsonResult = new JsonBuilder()
        jsonResult(getJsonForOptions(item).find { it.name == option }.value)
        return jsonResult.toString()
    }

    private static String getAndFormatCurrentContent(def node, String type) {
        if (type == 'date' || type == 'number') {
            return node.text()
        } else if (type == 'list') {
            def selectedOptions = node['@sd-selectedoptions'].text().trim()
            def optionsValue = new String(selectedOptions.decodeBase64())
            def jsonResult = new JsonBuilder()
            if (optionsValue)
                jsonResult(new JsonSlurper().parseText(optionsValue)[0].value)
            else {
                jsonResult()
            }
            return jsonResult.toString()
        } else if (type == 'richtext') {
            def value = node.text()
            value = new String(value.decodeBase64())
            return value;
        } else
            return "Don't know $type"
    }

    static String transform(Closure<String> transformer, def item, String contentValue) {
        if (transformer) {
            if (transformer.maximumNumberOfParameters == 2) {
                return transformer.call(item, contentValue)
            } else if (transformer.maximumNumberOfParameters == 1) {
                return transformer.call(item)
            } else {
                return transformer.call()
            }
        }
        return null
    }

}
