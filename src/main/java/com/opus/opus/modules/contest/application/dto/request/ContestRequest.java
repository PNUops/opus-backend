package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContestRequest(

        @NotBlank(message = "대회명은 비어 있을 수 없습니다.")
        String contestName,
        @NotNull(message = "카테고리ID는 비어 있을 수 없습니다.")
        Long categoryId
) {
}
