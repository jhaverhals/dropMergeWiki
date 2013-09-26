// static methods
public UpdateWikiProperties loadProperties(String... files) {
	def p = new Properties()
	files.each {
		File f1 = new File(it)
		if (f1.exists()) p.load(f1.newInputStream())
	}
	final String propPrefix = this.class.name
	System.getenv().each { prop ->
		['.', '_'].each { sep ->
			if (prop.key.startsWith(propPrefix + sep)) p[prop.key[propPrefix.length() + 1..-1]] = prop.value
		}
	}

	return new UpdateWikiProperties(p)
}

// script
UpdateWikiProperties props = loadProperties('team.properties', 'user.properties', 'session.properties')

CordysWiki wiki = new CordysWiki();
wiki.authenticate(props.wikiUserName, props.wikiPassword)
wiki.updateDropMergePage(
		props.wikiDropMergePageId,
		TransformerProvider.loadTransformers(props.transformerProvider, props),
		props.updateRealServer.toBoolean()
		)
