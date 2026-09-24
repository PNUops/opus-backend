package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CategoryContestSortCustomRequest(

        @NotNull(message = "대회 ID를 입력해주세요.")
        Long contestId,

        @NotNull(message = "변경된 아이템 순서를 입력해주세요.")
        Integer itemOrder
) {
}
