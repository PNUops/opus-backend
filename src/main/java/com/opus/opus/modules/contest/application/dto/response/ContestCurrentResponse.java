package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record ContestCurrentResponse(
        Long contestId,
        String categoryName,
        String contestName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        Long bannerId
) {
}
