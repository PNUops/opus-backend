package com.opus.opus.modules.contest.application.dto.response;

public record DownloadTargetResponse(

        Long submissionTypeId,
        String submissionTypeName,
        Long trackId,
        String trackName,
        Integer submittedTeamCount,
        Long estimatedSize
) {
}
