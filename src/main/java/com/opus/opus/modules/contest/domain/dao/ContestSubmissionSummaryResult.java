package com.opus.opus.modules.contest.domain.dao;

public record ContestSubmissionSummaryResult(

        long totalTeams,
        long submittedCount,
        long notSubmittedCount,
        long lateCount
) {
}
