package com.opus.opus.modules.team.domain.dao;

public record MyProjectFlatResult(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        String awardName,
        String awardColor
) {
}
