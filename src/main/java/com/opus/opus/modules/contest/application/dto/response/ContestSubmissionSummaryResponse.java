package com.opus.opus.modules.contest.application.dto.response;

public record ContestSubmissionSummaryResponse(
        long totalItemCount,
        long submittedCount,
        long totalFeedbackCount
) {
}
