package com.opus.opus.modules.team.domain.dao;

public record TeamSubmissionResult(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        boolean isSubmitted
) {
}
