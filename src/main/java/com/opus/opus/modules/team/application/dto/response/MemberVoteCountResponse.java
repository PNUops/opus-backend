package com.opus.opus.modules.team.application.dto.response;

public record MemberVoteCountResponse(
        Long remainingVotesCount,
        Long maxVotesLimit
) {
}
