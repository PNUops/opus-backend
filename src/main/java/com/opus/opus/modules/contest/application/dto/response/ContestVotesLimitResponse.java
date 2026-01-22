package com.opus.opus.modules.contest.application.dto.response;

public record ContestVotesLimitResponse(
        Integer maxVotesLimit
) {
    public static ContestVotesLimitResponse from(final Integer maxVotesLimit) {
        return new ContestVotesLimitResponse(maxVotesLimit);
    }
}
