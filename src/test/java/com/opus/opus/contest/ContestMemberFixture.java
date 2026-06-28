package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import java.util.ArrayList;
import java.util.List;

public class ContestMemberFixture {

    public static ContestMember createContestMember(final Contest contest, final Long memberId,
                                                    final List<Long> teamIds) {
        return ContestMember.builder()
                .contest(contest)
                .memberId(memberId)
                .teamIds(new ArrayList<>(teamIds))
                .build();
    }
}
