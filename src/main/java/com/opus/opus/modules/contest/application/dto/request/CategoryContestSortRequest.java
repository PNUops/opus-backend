package com.opus.opus.modules.contest.application.dto.request;

import com.opus.opus.modules.contest.domain.SidebarSortType;
import jakarta.validation.constraints.NotNull;

public record CategoryContestSortRequest(

        @NotNull(message = "모드를 입력하세요.")
        SidebarSortType mode
) {
}
