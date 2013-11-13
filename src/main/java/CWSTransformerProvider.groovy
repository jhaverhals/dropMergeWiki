import com.opentext.dropmerge.*

import static TestCount.Fail
import static TestCount.Pass

public class CWSTransformerProvider extends TransformerProvider {
    private static final BUILDMASTER_NL = new Jenkins('http://buildmaster-nl.vanenburg.com/jenkins')

    private static final JenkinsJob JUNIT_LINUX = BUILDMASTER_NL.withJob('cws-wip-junit-l')
    private static final JenkinsJob JUNIT_W = BUILDMASTER_NL.withJob('cws-wip-junit-w')
    private static final JenkinsJob GMF_C = BUILDMASTER_NL.withJob('cws-wip-gmf-chrome')
    private static final JenkinsJob GMF_F = BUILDMASTER_NL.withJob('cws-wip-gmf-ff')
    private static final JenkinsJob GMF_S = BUILDMASTER_NL.withJob('cws-wip-gmf-safari')

    final fieldFromPreviousPage = ['SuccesfulTests', 'FailedTests', 'MBViolationsHigh', 'MBViolationsMedium', 'CompilerWarnings', 'PMDViolationsHigh', 'PMDViolationsMedium']

    @Override
    Map<String, Closure<String>> getTransformer(UpdateWikiProperties props) {
        def transformers = [
                Team: { item -> CordysWiki.selectOption(item, 'CWS') },

                ProductManagerName: { getUserLink('hkastenb', 'Harmen Kastenberg') },
                ArchitectName: { ' ' + getUserLink('rprins', 'Rene Prins') },
                ScrumMasterName: { ' ' + getUserLink('rprins', 'Rene Prins') },

                SuccesfulTests: { JUNIT_LINUX.getTestFigure(Pass) },
                FailedTests: { JUNIT_LINUX.getTestFigure(Fail) },

                SuccessfulRegressionTestsComment: withTable { WikiTableBuilder table ->
                    table.addRow(Type: 'JUnit', OS: 'Linux', Successfull: JUNIT_LINUX.getTestFigure(Pass), Failures: JUNIT_LINUX.getTestFigure(Fail), Explanation: '')
                    table.addRow(Type: 'JUnit', OS: 'Windows', Successfull: JUNIT_W.getTestFigure(Pass), Failures: JUNIT_W.getTestFigure(Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Chrome', Successfull: GMF_C.getTestFigure(Pass), Failures: GMF_C.getTestFigure(Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Firefox', Successfull: GMF_F.getTestFigure(Pass), Failures: GMF_F.getTestFigure(Fail), Explanation: '')
                    table.addRow(Type: 'GMF', OS: 'Safari', Successfull: GMF_S.getTestFigure(Pass), Failures: GMF_S.getTestFigure(Fail), Explanation: '')
                },
        ]

        transferFromPreviousPage(props, props.previousWikiDropMergePageId, fieldFromPreviousPage, transformers)

        return transformers
    }

}