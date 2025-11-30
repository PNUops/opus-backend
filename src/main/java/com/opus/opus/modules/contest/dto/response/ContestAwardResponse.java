package com.opus.opus.modules.contest.dto.response;

import com.opus.opus.modules.contest.domain.ContestAward;

public record ContestAwardResponse(
        Long awardId,
        String awardName,
        String awardColor,
        Long contestId
) {

    public ContestAwardResponse(ContestAward contestAward) {
        this(
                contestAward.getId(),
                contestAward.getAwardName(),
                contestAward.getAwardColor(),
                contestAward.getContest().getId()
        );
    }
}
