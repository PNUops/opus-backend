package com.opus.opus.modules.contest.application.dto.response;

public record DownloadTargetResponse(

        Long submissionItemId,
        String submissionItemName,
        Long trackId,
        String trackName,
        Integer submittedTeamCount,
        Long estimatedSize
) {
}
