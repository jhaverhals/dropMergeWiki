public enum TestCount implements Jenkins.JenkinsJsonField {
    Pass, Fail, Total, Skip

    @Override
    String allValues() {
        values().collect { it.jsonField }.join(',')
    }

    @Override
    String getJsonField() {
        this.name().toLowerCase() + 'Count'
    }
}