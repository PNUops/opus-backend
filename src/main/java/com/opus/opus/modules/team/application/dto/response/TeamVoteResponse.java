package com.opus.opus.modules.team.application.dto.response;

public record TeamVoteResponse(
        Long remainingVotesCount,
        Long maxVotesLimit
) {
    public static TeamVoteResponse of(final long currentVoteCount, final long maxVotesLimit) {
        final long remainingVotesCount = maxVotesLimit - currentVoteCount;
        return new TeamVoteResponse(remainingVotesCount, maxVotesLimit);
    }
}
