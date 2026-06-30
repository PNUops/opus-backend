package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.dao.ContestSubmissionSummaryResult;

public record ContestSubmissionSummaryResponse(

        long totalTeams,
        long submittedCount,
        long notSubmittedCount,
        long lateCount
) {
    public static ContestSubmissionSummaryResponse of(final ContestSubmissionSummaryResult result) {
        return new ContestSubmissionSummaryResponse(
                result.totalTeams(),
                result.submittedCount(),
                result.notSubmittedCount(),
                result.lateCount()
        );
    }
}