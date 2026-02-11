package com.opus.opus.modules.contest.application.dto.response;

public record ContestRankingResponse(
        Integer rank,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Long voteCount
) {
    public ContestRankingResponse(Long teamId, String teamName, String projectName, String trackName, Long voteCount) { // Repository용 생성자
        this(null, teamId, teamName, projectName, trackName, voteCount);
    }
}
