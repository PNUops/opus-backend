package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContestCategoryRequest(

        @NotBlank(message = "카테고리명을 입력해주세요.")
        String categoryName
) {
}
