com.opentext.dropmerge.dsl.TestJobComparison.compare {

    forTeam('BAM') {
        withJob { job 'BAMTestOracle' on buildMasterHYD }
        andJob { job 'BAM_Modeler_JUnits' on buildMasterHYD }
        comparedToJob { job 'BAM-L' on jenkinsOfSVT }
    }

    forTeam('CWS') {
        withJob { job 'cws-wip-junit-l' on buildMasterNL; description 'Linux' }
        comparedToJob { job 'CWS-L' on jenkinsOfSVT; description 'Linux' }
    }

    forTeam('PCT') {
        withJob { job 'pct-trunk-wip-frt-l-x64' on buildMasterNL }
        comparedToJob { job 'PlatformCore-L' on jenkinsOfSVT }
    }

}
