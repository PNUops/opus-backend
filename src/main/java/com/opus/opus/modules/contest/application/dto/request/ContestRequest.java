package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContestRequest(
        @NotBlank(message = "대회명은 비어 있을 수 없습니다.")
        String contestName,
        @NotBlank(message = "카테고리ID는 비어 있을 수 없습니다.")
        Long categoryId
) {
}
