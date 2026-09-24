package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.SidebarSortType;

public record SidebarCategorySortResponse(
        SidebarSortType mode
) {
}
