package com.opus.opus.modules.contest.domain.dao;

import java.time.LocalDateTime;

public record ContestSubmissionStatusResult(

        Long submissionId,
        Long teamId,
        String teamName,
        String trackName,
        String submissionItemName,
        LocalDateTime firstSubmittedAt,
        LocalDateTime lastModifiedAt,
        LocalDateTime deadline
) {
}
