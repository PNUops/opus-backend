package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTemplate;

public class ContestTemplateFixture {

    public static ContestTemplate createContestTemplate(final Contest contest) {
        return ContestTemplate.builder()
                .contest(contest)
                .trackRequired(true)
                .projectNameRequired(true)
                .teamNameRequired(true)
                .leaderRequired(true)
                .teamMembersRequired(true)
                .professorRequired(true)
                .githubPathRequired(true)
                .youTubePathRequired(true)
                .productionPathRequired(true)
                .overviewRequired(true)
                .posterRequired(true)
                .imagesRequired(true)
                .build();
    }
}
