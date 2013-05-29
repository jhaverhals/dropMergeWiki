import groovy.xml.MarkupBuilder

class WikiTableBuilder {
    private final MarkupBuilder markupBuilder
    private List<String> headers = []
    private List<Map<String, String>> rows = []

    WikiTableBuilder() {
        this(new MarkupBuilder())
    }

    WikiTableBuilder(MarkupBuilder markupBuilder) {
        this.markupBuilder = markupBuilder
    }

    WikiTableBuilder(Writer writer) {
        this.markupBuilder = new MarkupBuilder(writer)
    }

    WikiTableBuilder(IndentPrinter writer) {
        this.markupBuilder = new MarkupBuilder(writer)
    }

    void setHeaders(List<String> headers) {
        this.headers = headers
    }

    void addRow(Map<String, String> values) {
        values.keySet().each {
            if (!headers.contains(it))
                headers.add(it)
        }
        rows.add(values)
    }

    void addRow(List<String> values) {
        Map<String, String> m = new HashMap<>()
        values.eachWithIndex { String entry, int i ->
            m[headers[i]] = entry
        }
        rows.add(m)
    }


    public void process() {
        if (!headers.isEmpty()) {
            markupBuilder.table(class: 'confluenceTable') {
                tbody {
                    tr {
                        headers.each { header ->
                            th(class: 'confluenceTh', header)
                        }
                    }
                    rows.each { map ->
                        tr {
                            headers.each { header ->
                                td(class: 'confluenceTd', map[header])
                            }
                        }
                    }
                }
            }

        }
    }
}
