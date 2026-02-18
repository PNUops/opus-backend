package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.SortType;

public record ContestSortResponse(

        SortType currentMode
) {
}
