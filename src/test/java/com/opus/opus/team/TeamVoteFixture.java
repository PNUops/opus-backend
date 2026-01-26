package com.opus.opus.team;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;

public class TeamVoteFixture {

    public static TeamVote createTeamVote(final Team team, final Long memberId, final Boolean isVoted) {
        return TeamVote.builder()
                .team(team)
                .memberId(memberId)
                .isVoted(isVoted)
                .build();
    }
}
