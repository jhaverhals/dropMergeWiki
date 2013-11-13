package com.opentext.dropmerge

public class UpdateWikiProperties {

    private Properties inner;
    private static final List<String> requiredProperties = ['wikiUserName', 'wikiPassword', 'wikiDropMergePageId', 'transformerProvider', 'updateRealServer']

    UpdateWikiProperties(Properties inner) {
        this.inner = inner
    }

    @Override
    public String getAt(String key) {
        inner[key]
    }

    @Override
    public String getProperty(String key) {
        validate()
        getAt(key)
    }

    public void validate() {
        requiredProperties.each {
            assert this[it]: "$it is a required property, but is undefined"
        }
    }
}
