package com.opus.opus.modules.contest.domain.dao;

import java.time.LocalDateTime;

public record TeamSubmissionStatusResult(

        Long submissionItemId,
        Long submissionId,
        String submissionItemName,
        String description,
        LocalDateTime deadlineAt,
        LocalDateTime firstSubmittedAt
) {
}
