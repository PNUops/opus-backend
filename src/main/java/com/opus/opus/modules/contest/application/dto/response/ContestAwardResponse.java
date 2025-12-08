package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestAward;

public record ContestAwardResponse(

        Long awardId,
        String awardName,
        String awardColor
) {

    public static ContestAwardResponse from(final ContestAward contestAward) {
        return new ContestAwardResponse(
                contestAward.getId(),
                contestAward.getAwardName(),
                contestAward.getAwardColor()
        );
    }
}
