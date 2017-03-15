package com.opentext.dropmerge.dsl

import com.opentext.dropmerge.CordysWiki


class QualityAndProcessQuestionsSpec {

    private Map<String, Closure<String>> inputs

    QualityAndProcessQuestionsSpec(Map<String, Closure<String>> inputs) {
        this.inputs = inputs
    }

    public void performanceDegraded(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('PerformanceDegradation', [yes, no, notTested], answer, comment)
    }

    public void performanceDegraded(ComboBoxAnswers answer, String comment) {
        performanceDegraded(answer, { withText comment })
    }

    public void xmlMemoryManagementIssuesIntroduced(ComboBoxAnswers answer,
                                                    @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('MemoryLeaksIntroduced', [yes, no, notTested], answer, comment)
    }

    public void xmlMemoryManagementIssuesIntroduced(ComboBoxAnswers answer, String comment) {
        xmlMemoryManagementIssuesIntroduced(answer, { withText comment })
    }

    public void regressionTestsPassWithPayloadValidation(ComboBoxAnswers answer,
                                                        @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('RegressionTestsPassWithPayloadValidation', [yes, no, notTested], answer, comment)
    }

    public void regressionTestsPassWithPayloadValidation(ComboBoxAnswers answer, String comment) {
        regressionTestsPassWithPayloadValidation(answer, { withText comment })
    }

    public void compliantWithHorizontalComponentRequirements(ComboBoxAnswers answer,
                                                             @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('CompliantWithHorizontalComponentRequirements', [yes, no, partially], answer, comment)
    }

    public void compliantWithHorizontalComponentRequirements(ComboBoxAnswers answer, String comment) {
        compliantWithHorizontalComponentRequirements(answer, { withText comment })
    }

    public void documentationReviewed(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('DocumentationReviewed', [yes, no, notApplicable], answer, comment)
    }

    public void documentationReviewed(ComboBoxAnswers answer, String comment) {
        documentationReviewed(answer, { withText comment })
    }

    public void messagesTranslatable(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('TranslatableMessages', [yes, no, notApplicable], answer, comment)
    }

    public void messagesTranslatable(ComboBoxAnswers answer, String comment) {
        messagesTranslatable(answer, { withText comment })
    }

    public void defectFixesRetestedByOtherPerson(ComboBoxAnswers answer,
                                                 @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('DefectFixesRetestedByOtherPerson', [yes, no, notApplicable], answer, comment)
    }

    public void defectFixesRetestedByOtherPerson(ComboBoxAnswers answer, String comment) {
        defectFixesRetestedByOtherPerson(answer, { withText comment })
    }

    public void alertsDocumented(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('DocumentedAlerts', [yes, no, notApplicable], answer, comment)
    }

    public void alertsDocumented(ComboBoxAnswers answer, String comment) {
        alertsDocumented(answer, { withText comment })
    }

    public void multiPlatformValidationDone(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('MultiplatformValidationDone', [yes, no, notApplicable], answer, comment)
    }

    public void multiPlatformValidationDone(ComboBoxAnswers answer, String comment) {
        multiPlatformValidationDone(answer, { withText comment })
    }

    public void forwardPortingCompleted(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('ForwardPortingCompleted', [yes, no, notApplicable], answer, comment)
    }

    public void forwardPortingCompleted(ComboBoxAnswers answer, String comment) {
        forwardPortingCompleted(answer, { withText comment })
    }

    public void migrationAspectsHandled(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('MigrationAspectsHandled', [yes, no, notApplicable], answer, comment)
    }

    public void migrationAspectsHandled(ComboBoxAnswers answer, String comment) {
        migrationAspectsHandled(answer, { withText comment })
    }

    public void backwardCompatibilityIssuesIntroduced(ComboBoxAnswers answer,
                                                      @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('BackwardCompatibilityIssues', [yes, no], answer, comment)
    }

    public void backwardCompatibilityIssuesIntroduced(ComboBoxAnswers answer, String comment) {
        backwardCompatibilityIssuesIntroduced(answer, { withText comment })
    }

    public void usabilityAcceptedByPM(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('UsabilityAcceptedByPM', [yes, no], answer, comment)
    }

    public void usabilityAcceptedByPM(ComboBoxAnswers answer, String comment) {
        usabilityAcceptedByPM(answer, { withText comment })
    }

    public void userStoriesAcceptedByPM(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('UserStoriesAcceptedByPM', [yes, no], answer, comment)
    }

    public void userStoriesAcceptedByPM(ComboBoxAnswers answer, String comment) {
        userStoriesAcceptedByPM(answer, { withText comment })
    }

    public void buildModelersSucceeds(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('BuildModelersSucceeds', [yes, no], answer, comment) 
    }

    public void buildModelersSucceeds(ComboBoxAnswers answer, String comment) {
        buildModelersSucceeds(answer, { withText comment })
    }

    public void fullDropMerge(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('FullDropMerge', [yes, no], answer, comment) 
    }

    public void fullDropMerge(ComboBoxAnswers answer, String comment) {
        fullDropMerge(answer, { withText comment })
    }

    public void securityIssuesIntroduced(ComboBoxAnswers answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('SecurityIssuesIntroduced', [yes, no, notTested], answer, comment)
    }

    public void securityIssuesIntroduced(ComboBoxAnswers answer, String comment) {
        securityIssuesIntroduced(answer, { withText comment })
    }

    public void buildAndInstallerChangesAddressed(ComboBoxAnswers answer,
                                                  @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('BuildAndInstallerChangesAddressed', [yes, no, notApplicable], answer, comment)
    }

    public void buildAndInstallerChangesAddressed(ComboBoxAnswers answer, String comment) {
        buildAndInstallerChangesAddressed(answer, { withText comment })
    }

    public void newManualTestCasesAdded(ComboBoxAnswers answer, String comment) {
        newManualTestCasesAdded(answer.optionText, { withText comment })
    }

    public void newManualTestCasesAdded(String answer, @DelegatesTo(FreeTextSpec) Closure comment = null) {
        addInput('NewManualTestCases', answer, comment)
    }

    private void addInput(String fieldName, List<ComboBoxAnswers> allowed, ComboBoxAnswers answer,
                          @DelegatesTo(FreeTextSpec) Closure comment) {
        if (!allowed.contains(answer))
            throw new IllegalArgumentException("answer for $fieldName must be one of these: $allowed")

        inputs[fieldName] = { item -> CordysWiki.selectOption(item, answer.optionText) }
        if (comment) {
            FreeTextSpec freeTextSpec = new FreeTextSpec()
            freeTextSpec.with comment
            inputs[fieldName + 'Comment'] = { freeTextSpec.text }
        }
    }

    private void addInput(String fieldName, String answer, @DelegatesTo(FreeTextSpec) Closure comment) {
        inputs[fieldName] = { answer }
        if (comment) {
            FreeTextSpec freeTextSpec = new FreeTextSpec()
            freeTextSpec.with comment
            inputs[fieldName + 'Comment'] = { freeTextSpec.text }
        }
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
