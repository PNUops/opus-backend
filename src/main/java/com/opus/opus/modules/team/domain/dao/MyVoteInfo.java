package com.opus.opus.modules.team.domain.dao;

public record MyVoteInfo(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName
) {
}
