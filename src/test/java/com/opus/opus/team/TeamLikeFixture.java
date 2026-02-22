package com.opus.opus.team;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;

public class TeamLikeFixture {

    public static TeamLike createTeamLike(final Team team, final Long memberId, final Boolean isLiked) {
        return TeamLike.builder()
                .team(team)
                .memberId(memberId)
                .isLiked(isLiked)
                .build();
    }
}
