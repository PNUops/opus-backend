package com.opus.opus.team;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;

public class TeamCommentFixture {

    public static TeamComment createTeamComment(final Team team, final Long memberId) {
        return TeamComment.builder()
                .description("테스트용 댓글입니다.")
                .memberId(memberId)
                .team(team)
                .build();
    }
}
