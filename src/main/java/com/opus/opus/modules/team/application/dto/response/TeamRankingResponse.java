package com.opus.opus.modules.team.application.dto.response;

public record TeamRankingResponse(
        Integer rank,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Long voteCount
) {
    public TeamRankingResponse(Long teamId, String teamName, String projectName, String trackName, Long voteCount) { // Repository용 생성자
        this(null, teamId, teamName, projectName, trackName, voteCount);
    }
}
