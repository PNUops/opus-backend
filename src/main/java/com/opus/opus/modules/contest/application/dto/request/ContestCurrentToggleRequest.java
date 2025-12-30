package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContestCurrentToggleRequest(
        @NotNull(message = "현재 진행 여부를 입력해주세요.")
        Boolean isCurrent
) {
}
