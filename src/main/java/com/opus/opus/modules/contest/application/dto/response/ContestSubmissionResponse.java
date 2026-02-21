package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.domain.Team;

public record ContestSubmissionResponse(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Boolean isSubmitted
) {
    public static ContestSubmissionResponse from(Team team, String trackName) {
        return new ContestSubmissionResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                trackName,
                team.getIsSubmitted()
        );
    }
}
