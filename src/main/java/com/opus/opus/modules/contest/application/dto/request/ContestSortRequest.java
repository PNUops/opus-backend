package com.opus.opus.modules.contest.application.dto.request;

import com.opus.opus.modules.contest.domain.SortType;
import jakarta.validation.constraints.NotNull;

public record ContestSortRequest(

        @NotNull(message = "모드를 입력하세요.")
        SortType mode
) {
}
