package com.opus.opus.modules.contest.application.dto.response;

public record ArchiveTargetResponse(

        Long submissionTypeId,
        String submissionTypeName,
        Long trackId,
        String trackName,
        Integer submittedTeamCount,
        Long estimatedSize
) {
}
