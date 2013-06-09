@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.6')
import CordysWiki
import TransformerProvider

// static methods
public static void loadProperties(Properties props, File f) {
    if (f.exists()) props.load(f.newInputStream())
}

public static Properties loadProperties(String... files) {
    def p = new Properties()
    files.each { loadProperties(p, new File(it)) }
    return p
}

// script
CordysWiki wiki = new CordysWiki();
Properties props = loadProperties('team.properties', 'user.properties', 'session.properties')

assert props.wikiUserName
assert props.wikiPassword
assert props.transformerProvider

wiki.authenticate(props.wikiUserName, props.wikiPassword)
wiki.updateDropMergePage(props.wikiDropMergePageId, TransformerProvider.loadTransformers(props.transformerProvider, props), false)