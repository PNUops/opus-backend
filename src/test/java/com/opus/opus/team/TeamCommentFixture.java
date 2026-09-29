package com.opus.opus.team;

import static com.opus.opus.modules.team.domain.TeamCommentVisibility.PUBLIC;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.TEAM;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;

public class TeamCommentFixture {

    public static TeamComment createTeamComment(final Team team, final Long memberId) {
        return TeamComment.builder()
                .description("테스트용 댓글입니다.")
                .memberId(memberId)
                .team(team)
                .visibility(PUBLIC)
                .build();
    }

    public static TeamComment createTeamOnlyComment(final Team team, final Long memberId) {
        return TeamComment.builder()
                .description("테스트용 팀 피드백입니다.")
                .memberId(memberId)
                .team(team)
                .visibility(TEAM)
                .build();
    }
}
