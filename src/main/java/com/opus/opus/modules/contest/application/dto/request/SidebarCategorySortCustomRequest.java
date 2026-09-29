package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record SidebarCategorySortCustomRequest(

        @NotNull(message = "카테고리 ID를 입력해주세요.")
        Long categoryId,

        @NotNull(message = "변경된 아이템 순서를 입력해주세요.")
        Integer itemOrder
) {
}
