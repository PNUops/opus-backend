package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSort;

public class ContestSortFixture {

    public static ContestSort createContestSort(final Contest contest) {
        return ContestSort.builder().contest(contest).build();
    }
}
