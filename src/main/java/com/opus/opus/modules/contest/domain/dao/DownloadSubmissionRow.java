package com.opus.opus.modules.contest.domain.dao;

public record DownloadSubmissionRow(

        Long submissionItemId,
        Long trackId,
        String teamName,
        Long submissionId
) {
}
