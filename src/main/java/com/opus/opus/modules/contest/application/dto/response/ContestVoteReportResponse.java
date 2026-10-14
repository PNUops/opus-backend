package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ContestVoteReportResponse(

        String contestName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        Boolean isVoteEnded,
        LocalDateTime aggregatedAt,
        Integer maxVotesLimit,
        Long totalVoters,
        Long totalVotes,
        Double averageVotesPerVoter,
        Integer teamCount,
        Double averageVotesPerTeam,
        Integer zeroVoteTeamCount,
        List<DailyVoteResponse> dailyVotes,
        List<VoteUsageResponse> voteUsages,
        List<HourlyVoteResponse> hourlyVotes,
        List<TrackVoteResponse> trackVotes,
        List<TrackRankingResponse> trackTopRankings,
        List<ContestRankingResponse> rankings
) {
}
