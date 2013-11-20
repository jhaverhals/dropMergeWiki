package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.CordysWiki


class QualityAndProcessQuestionsSpec {

    private Map<String, Closure<String>> inputs

    QualityAndProcessQuestionsSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    public void performanceDegraded(ComboBoxAnswers answer, String comment = null) {
        addInput('PerformanceDegradation', [yes, no, notTested], answer, comment)
    }

    public void xmlMemoryManagementIssues(ComboBoxAnswers answer, String comment = null) {
        addInput('MemoryLeaksIntroduced', [yes, no, notTested], answer, comment)
    }

    public void regressionTestPassWithPayloadValidation(ComboBoxAnswers answer, String comment = null) {
        addInput('RegressionTestsPassWithPayloadValidation', [yes, no, notTested], answer, comment)
    }

    public void compliantWithHorizontalComponentRequirements(ComboBoxAnswers answer, String comment = null) {
        addInput('CompliantWithHorizontalComponentRequirements', [yes, no, partially], answer, comment)
    }

    public void documentationReviewed(ComboBoxAnswers answer, String comment = null) {
        addInput('DocumentationReviewed', [yes, no, notApplicable], answer, comment)
    }

    public void messagesTranslatable(ComboBoxAnswers answer, String comment = null) {
        addInput('TranslatableMessages', [yes, no, notApplicable], answer, comment)
    }

    public void defectFixesRetestedByOtherPerson(ComboBoxAnswers answer, String comment = null) {
        addInput('DefectFixesRetestedByOtherPerson', [yes, no, notApplicable], answer, comment)
    }

    public void documentedAlerts(ComboBoxAnswers answer, String comment = null) {
        addInput('DocumentedAlerts', [yes, no, notApplicable], answer, comment)
    }

    public void multiPlatformValidationDone(ComboBoxAnswers answer, String comment = null) {
        addInput('MultiplatformValidationDone', [yes, no, notApplicable], answer, comment)
    }

    def completedForwardPorting(ComboBoxAnswers answer, String comment = null) {
        addInput('ForwardPortingCompleted', [yes, no, notApplicable], answer, comment)
    }

    def migrationAspectsHandled(ComboBoxAnswers answer, String comment = null) {
        addInput('MigrationAspectsHandled', [yes, no, notApplicable], answer, comment)
    }

    def backwardCompatibilityIssues(ComboBoxAnswers answer, String comment = null) {
        addInput('BackwardCompatibilityIssues', [yes, no], answer, comment)
    }

    def introducedSecurityIssues(ComboBoxAnswers answer, String comment = null) {
        addInput('SecurityIssuesIntroduced', [yes, no, notTested], answer, comment)
    }

    def buildAndInstallerChangesAddressed(ComboBoxAnswers answer, String comment = null) {
        addInput('BuildAndInstallerChangesAddressed', [yes, no, notApplicable], answer, comment)
    }

    def newManualTestCassesAdded(String answer, String comment = null) {
        addInput('NewManualTestCases', answer, comment)
    }

    private void addInput(String fieldName, List<ComboBoxAnswers> allowed, ComboBoxAnswers answer, String comment) {
        if (!allowed.contains(answer))
            throw new IllegalArgumentException('answer for ' + fieldName + ' must be one of these: ' + allowed)

        inputs[fieldName] = { item -> CordysWiki.selectOption(item, answer.optionText) }
        if (comment)
            inputs[fieldName + 'Comment'] = { comment }
    }

    private void addInput(String fieldName, String answer, String comment) {
        inputs[fieldName] = { answer }
        if (comment)
            inputs[fieldName + 'Comment'] = { comment }
    }

    enum ComboBoxAnswers implements AnswerEnum {
        Yes, No, Partially, NotTested, NotApplicable;

        @Override
        String getOptionText() {
            switch (this) {
                case Yes:
                    return 'Yes';
                case No:
                    return 'No';
                case NotTested:
                    return 'Not tested';
                case NotApplicable:
                    return 'Not applicable';
                case Partially:
                    return 'Partially (explain in comment)';
                default:
                    throw new IllegalStateException();
            }
        }
    }

    ComboBoxAnswers getYes() {
        ComboBoxAnswers.Yes
    }

    ComboBoxAnswers getNo() {
        ComboBoxAnswers.No
    }

    ComboBoxAnswers getNotTested() {
        ComboBoxAnswers.NotTested
    }

    ComboBoxAnswers getNotApplicable() {
        ComboBoxAnswers.NotApplicable
    }

    ComboBoxAnswers getPartially() {
        ComboBoxAnswers.Partially
    }
}
