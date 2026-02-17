package com.opus.opus.modules.contest.application.dto.response;

public record ContestSubmissionResponse(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Boolean isSubmitted
) {
}
