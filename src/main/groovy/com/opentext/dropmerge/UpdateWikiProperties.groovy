package com.opentext.dropmerge

public class UpdateWikiProperties {

    private Properties inner;
    private static final List<String> requiredProperties = ['wikiUserName', 'wikiPassword', 'wikiDropMergePageId', 'transformerProvider']

    UpdateWikiProperties(Properties inner) {
        this.inner = inner
    }

    public String getAt(String key) {
        inner[key]
    }

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
