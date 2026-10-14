package com.opus.opus.modules.contest.application.dto.response;

public record TrackVoteResponse(

        String trackName,
        Integer teamCount,
        Long voteCount,
        Double averageVotesPerTeam
) {
}
