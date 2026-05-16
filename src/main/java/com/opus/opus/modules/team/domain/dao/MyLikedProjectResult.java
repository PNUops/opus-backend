package com.opus.opus.modules.team.domain.dao;

public record MyLikedProjectResult(
        Long teamId,
        String teamName,
        String projectName,
        Long contestId,
        String contestName
) {
}
