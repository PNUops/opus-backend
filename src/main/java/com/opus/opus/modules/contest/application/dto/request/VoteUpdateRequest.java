package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record VoteUpdateRequest(
        @NotNull(message = "투표 시작 시각을 정해야 합니다")
        LocalDateTime voteStartAt,
        @NotNull(message = "투표 종료 시각을 정해야 합니다")
        LocalDateTime voteEndAt
) {
}
