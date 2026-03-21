package com.opus.opus.modules.member.domain.dao;

public record MyVoteResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName
) {
}
