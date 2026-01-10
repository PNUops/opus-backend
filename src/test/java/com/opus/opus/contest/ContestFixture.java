package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;

public class ContestFixture {

    public static Contest createContest() {
        return Contest.builder()
                .contestName("제 1회 테스트 대회")
                .categoryId(1L)
                .build();
    }

    public static Contest createContest(String contestName, Long categoryId) {
        return Contest.builder()
                .contestName(contestName)
                .categoryId(categoryId)
                .build();
    }
}
