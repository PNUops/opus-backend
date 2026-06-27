package com.opus.opus.modules.contest.domain.dao;

public record DownloadFileRow(

        Long submissionTypeId,
        Long trackId,
        String teamName,
        String fileName,
        String filePath
) {
}
