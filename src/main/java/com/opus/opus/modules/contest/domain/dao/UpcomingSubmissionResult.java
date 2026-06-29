package com.opus.opus.modules.contest.domain.dao;

import java.time.LocalDateTime;

public record UpcomingSubmissionResult(

        Long submissionItemId,
        Long submissionId,
        String submissionItemName,
        LocalDateTime deadlineAt,
        LocalDateTime lastModifiedAt,
        LocalDateTime firstSubmittedAt
) {
}
