com.opentext.dropmerge.dsl.TestJobComparison.compare {

    forTeam('BAM') {
        withJob { job 'BAMTestOracle' on buildMasterHYD }
        andJob { job 'BAM_Modeler_JUnits' on buildMasterHYD }
        comparedToJob { job 'BAM-L' on jenkinsOfSVT }
    }

    forTeam('CWS') {
        withJob { job 'cws-wip-junit-l' on buildMasterNL }
        comparedToJob { job 'CWS-L' on jenkinsOfSVT }
    }

    forTeam('PCT') {
        withJob { job 'pct-trunk-wip-frt-l-x64' on buildMasterNL; description 'Linux' }
        comparedToJob { job 'PlatformCore-L' on jenkinsOfSVT; description 'Linux' }

        withJob { job 'pct-trunk-wip-frt-w-x64' on buildMasterNL; description 'Windows' }
        comparedToJob { job 'PlatformCore-W' on jenkinsOfSVT; description 'Windows' }
    }

}
