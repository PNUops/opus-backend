package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import java.util.List;

public record SidebarResponse(
        Long categoryId,
        String categoryName,
        List<ContestItem> contests
) {

    public static SidebarResponse of(ContestCategory category, List<Contest> contests) {
        return new SidebarResponse(
                category.getId(),
                category.getCategoryName(),
                contests.stream().map(ContestItem::from).toList()
        );
    }

    public record ContestItem(
            Long contestId,
            String contestName,
            Boolean isCurrent
    ) {
        public static ContestItem from(Contest contest) {
            return new ContestItem(contest.getId(), contest.getContestName(), contest.getIsCurrent());
        }

    }
}
