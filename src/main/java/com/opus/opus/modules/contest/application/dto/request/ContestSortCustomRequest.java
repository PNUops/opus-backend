package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContestSortCustomRequest(

        @NotNull(message = "팀 ID를 입력해주세요.")
        Long teamId,

        @NotNull(message = "변경된 아이템 순서를 입력해주세요.")
        Integer itemOrder
) {
}
