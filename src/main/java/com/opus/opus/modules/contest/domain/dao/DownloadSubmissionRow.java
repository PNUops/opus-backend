package com.opus.opus.modules.contest.domain.dao;

public record DownloadSubmissionRow(

        Long submissionTypeId,
        Long trackId,
        String teamName,
        Long submissionId
) {
}
