package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.domain.dao.TeamSubmissionResult;

public record ContestSubmissionResponse(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Boolean isSubmitted
) {
    public static ContestSubmissionResponse from(TeamSubmissionResult result) {
        return new ContestSubmissionResponse(
                result.teamId(),
                result.teamName(),
                result.projectName(),
                result.trackName(),
                result.isSubmitted()
        );
    }
}
