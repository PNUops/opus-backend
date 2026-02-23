package com.opus.opus.modules.contest.application.dto.response;

public record ContestRankingResponse(
        Integer rank,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Long voteCount
) {
}
