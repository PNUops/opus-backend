package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;

public class ContestTrackFixture {

    public static ContestTrack createTrack(final Contest contest) {
        return ContestTrack.builder()
                .trackName("테스트 분과")
                .contest(contest)
                .build();
    }
}
