public enum WarningLevel implements Jenkins.JenkinsJsonField {
    High, Normal

    @Override
    String allValues() {
        values().collect { it.jsonField }.join(',')
    }

    @Override
    String getJsonField() {
        'numberOf' + this.name() + 'PriorityWarnings'
    }
}