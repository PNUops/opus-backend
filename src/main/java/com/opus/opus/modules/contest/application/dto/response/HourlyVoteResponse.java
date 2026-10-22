package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record HourlyVoteResponse(

        LocalDateTime hour,
        Long voteCount,
        Long cumulativeVoterCount
) {
}
