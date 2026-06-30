package com.opus.opus.modules.contest.domain.dao;

public record DownloadTargetResult(

        Long submissionItemId,
        String submissionItemName,
        Long trackId,
        String trackName,
        Long submittedTeamCount,
        Long estimatedSize
) {
}
