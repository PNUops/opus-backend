package com.opus.opus.modules.contest.application.dto.response;

public record VoteUsageResponse(

        Long usedVotes,
        Long voterCount,
        Double ratio
) {
}
