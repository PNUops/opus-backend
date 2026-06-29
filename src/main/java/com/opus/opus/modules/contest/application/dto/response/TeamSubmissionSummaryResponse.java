package com.opus.opus.modules.contest.application.dto.response;

public record TeamSubmissionSummaryResponse(
        long totalItemCount,
        long submittedCount,
        long totalFeedbackCount
) {
}
