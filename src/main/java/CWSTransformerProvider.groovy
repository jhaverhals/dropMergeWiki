import static Jenkins.getTestFigure
import static TestCount.Fail
import static TestCount.Pass

public class CWSTransformerProvider extends TransformerProvider {
    private static final String JUNIT_LINUX = 'cws-wip-junit-l'
    private static final String JUNIT_W = 'cws-wip-junit-w'
    private static final String GMF_C = 'cws-wip-gmf-chrome'
    private static final String GMF_F = 'cws-wip-gmf-ff'
    private static final String GMF_S = 'cws-wip-gmf-safari'

    final String AFTER = "After"
    final String BEFORE = "Before"
    final fieldFromPreviousPage = ['SuccesfulTests', 'FailedTests', 'MBViolationsHigh', 'MBViolationsMedium', 'CompilerWarnings', 'PMDViolationsHigh', 'PMDViolationsMedium']

    @Override
    Map<String, Closure<String>> getTransformer(Properties props) {
        def transformers = [
                Team: { item -> selectOption(item, 'CWS') },

                ProductManagerName: { getUserLink('hkastenb', 'Harmen Kastenberg') },
                ArchitectName: { ' ' + getUserLink('rprins', 'Rene Prins') },
                ScrumMasterName: { ' ' + getUserLink('rprins', 'Rene Prins') },

                SuccesfulTests: { getTestFigure(JUNIT_LINUX, Pass) },
                FailedTests: { getTestFigure(JUNIT_LINUX, Fail) },

                SuccessfulRegressionTestsComment: withTable { WikiTableBuilder table ->
                    table.addRow(Type: 'JUnit', OS: 'Linux', Successfull: getTestFigure(JUNIT_LINUX, Pass), Failures: getTestFigure(JUNIT_LINUX, Fail), Explanation: '')
                    table.addRow(Type: 'JUnit', OS: 'Windows', Successfull: getTestFigure(JUNIT_W, Pass), Failures: getTestFigure(JUNIT_W, Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Chrome', Successfull: getTestFigure(GMF_C, Pass), Failures: getTestFigure(GMF_C, Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Firefox', Successfull: getTestFigure(GMF_F, Pass), Failures: getTestFigure(GMF_F, Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Safari', Successfull: getTestFigure(GMF_S, Pass), Failures: getTestFigure(GMF_S, Fail), Explanation: '')
                },
        ]



        CordysWiki wiki = new CordysWiki();
        wiki.authenticate(props.wikiUserName, props.wikiPassword)
        wiki.eachDropMergeField(props.previousWikiDropMergePageId) { CordysWiki.FormField formField ->
            if (formField.name.length() > AFTER.length() && fieldFromPreviousPage.contains(formField.name[0..-(AFTER.length() + 1)])) {
                transformers.put(formField.name[0..-(AFTER.length() + 1)] + BEFORE) { formField.content }
            }
        }

        return transformers
    }
}