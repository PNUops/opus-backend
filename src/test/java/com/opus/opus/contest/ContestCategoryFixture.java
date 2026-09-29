package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestCategory;

public class ContestCategoryFixture {

    public static ContestCategory createContestCategory() {
        return ContestCategory.builder()
                .categoryName("테스트 카테고리")
                .itemOrder(1)
                .build();
    }

    public static ContestCategory createContestCategoryWithName(final String categoryName) {
        return ContestCategory.builder()
                .categoryName(categoryName)
                .itemOrder(1)
                .build();
    }
}
