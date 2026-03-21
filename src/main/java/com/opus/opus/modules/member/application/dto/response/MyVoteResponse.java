package com.opus.opus.modules.member.application.dto.response;

public record MyVoteResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName
) {
}
