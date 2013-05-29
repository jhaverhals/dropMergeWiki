@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')

import TransformerProvider
import CordysWiki

// properties
def props = new Properties()
loadProperties(props, 'team.properties')
loadProperties(props, 'user.properties')
loadProperties(props, 'session.properties')

// static methods
public static void loadProperties(Properties props, String fileName) {
    def f = new File(fileName)
    if (!f.exists())
        return
    props.load(f.newInputStream())
}

private static Map<String, Closure<String>> getTransformer(Properties props) {
    GroovyClassLoader gcl = new GroovyClassLoader();
    Class clazz = gcl.parseClass(new File(props.transformerProvider))
    TransformerProvider ifc = (TransformerProvider) clazz.newInstance()
    return ifc.getTransformer(props)
}

// script

CordysWiki wiki = new CordysWiki();

wiki.authenticate(props.wikiUserName, props.wikiPassword)
wiki.updateDropMergePage(props.wikiDropMergePageId, getTransformer(props), false)