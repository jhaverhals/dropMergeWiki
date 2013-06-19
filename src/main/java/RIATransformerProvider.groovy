import static WarningLevel.High
import static WarningLevel.Normal

public class RIATransformerProvider extends TransformerProvider {
    private static final BUILDMASTER_HYD = new Jenkins('http://buildmaster-hyd.vanenburg.com/jenkins')
    private static final CMT_JENKINS = new Jenkins('http://cmt-jenkins.vanenburg.com/jenkins')

    private static final JenkinsJob TRUNK_MBV = CMT_JENKINS.withJob('MBV-RIA')
    private static final JenkinsJob MBV = BUILDMASTER_HYD.withJob('MBV-RIA')
    private static final JenkinsJob TRUNK_PMD = CMT_JENKINS.withJob('RIA')
    private static final JenkinsJob PMD = BUILDMASTER_HYD.withJob('RIA-FULLREPORT')

    @Override
    Map<String, Closure<String>> getTransformer(UpdateWikiProperties props) {
        def transformers = [
                Team: { item -> CordysWiki.selectOption(item, 'RIA') },

                MBViolationsHighBefore: { TRUNK_MBV.getMBFigure(High) },
                MBViolationsHighAfter: { MBV.getMBFigure(High) },
                MBViolationsMediumBefore: { TRUNK_MBV.getMBFigure(Normal) },
                MBViolationsMediumAfter: { MBV.getMBFigure(Normal) },

                PMDViolationsHighBefore: { TRUNK_PMD.getPMDFigure(High) },
                PMDViolationsHighAfter: { PMD.getPMDFigure(High) },
                PMDViolationsMediumBefore: { TRUNK_PMD.getPMDFigure(Normal) },
                PMDViolationsMediumAfter: { PMD.getPMDFigure(Normal) },
        ]

        return transformers
    }

}