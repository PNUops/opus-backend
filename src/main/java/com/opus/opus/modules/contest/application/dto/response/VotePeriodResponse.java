package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record VotePeriodResponse(
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt
) {
}
