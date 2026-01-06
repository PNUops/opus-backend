package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ContestVotesLimitRequest(
        @NotNull(message = "최대 투표 개수는 필수입니다.")
        @Min(value = 1, message = "최대 투표 개수는 1 이상이어야 합니다.")
        Integer maxVotesLimit
) {
}
