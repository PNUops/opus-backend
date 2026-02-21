package com.opus.opus.modules.team.domain.dao;

public record TeamRankingResult(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        Long voteCount
) {
}
