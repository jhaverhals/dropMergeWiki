import CordysWiki
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')

import TransformerProvider

// properties
def props = new Properties()
loadProperties(props, 'team.properties')
loadProperties(props, 'user.properties')
loadProperties(props, 'session.properties')

// static methods
public static void loadProperties(Properties props, String fileName) {
    def f = new File(fileName)
    if (f.exists()) props.load(f.newInputStream())
}

// script
CordysWiki wiki = new CordysWiki();

wiki.authenticate(props.wikiUserName, props.wikiPassword)
wiki.updateDropMergePage(props.wikiDropMergePageId, TransformerProvider.loadTransformers(props.transformerProvider, props), false)