package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;

public class ContestFixture {

    public static Contest createContest(final Long categoryId) {
        return Contest.builder()
                .contestName("테스트 대회")
                .categoryId(categoryId)
                .build();
    }
}
