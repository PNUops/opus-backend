package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDate;

public record DailyVoteResponse(

        LocalDate date,
        Long voterCount,
        Long voteCount
) {
}
