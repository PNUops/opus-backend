package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestCategory;

public class ContestCategoryFixture {

    public static ContestCategory createContestCategory() {
        return ContestCategory.builder()
                .categoryName("테스트 카테고리")
                .build();
    }
}

