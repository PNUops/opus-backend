package com.opus.opus.modules.team.application.dto.response;

import java.util.List;

public record TeamContestAwardResponse(
        List<AwardInfo> awards
) {
    public record AwardInfo(
            Long awardId,
            String awardName,
            String awardColor
    ) {
    }
}
