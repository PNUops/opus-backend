package com.opus.opus.modules.contest.application.dto.response;

public record ContestVoteStatisticsResponse(
        Long totalVotes,
        Long totalVoters,
        Double averageVotesPerVoter
) {
}
