package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.Team;

public record MyVoteResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName
) {
    public static MyVoteResponse of(final Team team, final String contestName) {
        return new MyVoteResponse(
                team.getContestId(),
                contestName,
                team.getId(),
                team.getTeamName(),
                team.getProjectName()
        );
    }
}
