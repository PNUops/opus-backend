package com.opus.opus.modules.contest.domain.dao;

public record ArchiveTargetResult(
        Long submissionTypeId,
        String submissionTypeName,
        Long trackId,
        String trackName,
        Long submittedTeamCount,
        Long estimatedSize
) {
}
