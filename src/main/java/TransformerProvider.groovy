import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

public abstract class TransformerProvider {
    private static final String JIRA_MACRO_PARAMS_PREFIX = 'renderMode=static|columns=type,key,summary,status|url=https://jira.cordys.com/jira/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery'

    public abstract Map<String, Closure<String>> getTransformer(Properties p);

    static String getUserLink(String shortName, String fullName) {
        Writer writer = new StringWriter()
        MarkupBuilder htmlBuilder = newMarkupBuilder(writer)

        htmlBuilder.a(
                'class': 'confluence-link confluence-userlink',
                'href': "/display/~$shortName",
                'username': shortName,
                'data-username': shortName,
                'data-linked-resource-type': 'userinfo',
                'data-linked-resource-default-alias': fullName,
                'data-base-url': 'https://wiki.cordys.com',
                fullName
        )

        return writer.toString()
    }

    /* WORK IN PROGRESS
    static String getJiraIssues(String jiraQuery) {
        Writer writer = new StringWriter()
        MarkupBuilder htmlBuilder = newMarkupBuilder(writer)
        String macro = '{jiraissues:'+JIRA_MACRO_PARAMS_PREFIX +'='+ jiraQuery.replace('=','%3D').replace(',','%2C') +'}'

        htmlBuilder.p {
            img(
                    'src': '/plugins/servlet/confluence/placeholder/macro?definition=' + macro.bytes.encodeBase64() +'&locale=en_GB&version=2',
                    'class': 'editor-inline-macro',
                    'data-macro-name': 'jiraissues',
                    'data-macro-parameters': JIRA_MACRO_PARAMS_PREFIX +'\\\\='+ jiraQuery.replace('=','%3D').replace(',','%2C')
            )
        }

        return writer.toString()
    }
    */

    static MarkupBuilder newMarkupBuilder(Writer writer) {
        return new MarkupBuilder(new IndentPrinter(writer, '', false))
    }

    static String selectOptionByStatus(def item, JenkinsJob job, Map<String, String> statusToOptionName) {
        String result = job.lastBuildResult
        if (statusToOptionName.containsKey(result)) {
            return CordysWiki.selectOption(item, statusToOptionName[result])
        } else {
            return null
        }
    }

    static Closure<String> withHtml(Closure<String> c) {
        return {
            Writer writer = new StringWriter()
            MarkupBuilder htmlBuilder = newMarkupBuilder(writer)
            c(htmlBuilder)
            return writer.toString()
        }
    }

    static Closure<String> withTable(Closure<String> c) {
        return withHtml { html ->
            WikiTableBuilder table = new WikiTableBuilder(html);
            c(table)
            table.process()
        }
    }

    public static void main(String[] args) {
        String jiraQuery = 'sprint = "PCT BOP 4.3 Sprint 6" AND resolution = Fixed AND issuetype not in ("Bug during story", Todo)';
        String v = ('{jiraissues:' + JIRA_MACRO_PARAMS_PREFIX + jiraQuery + '}')
        println v.bytes.encodeBase64()
    }
}
