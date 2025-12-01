package com.opus.opus.modules.team.dto.response;

import java.util.List;

public record TeamAwardResponse(
        List<AwardInfo> awards
) {
    public record AwardInfo(
            Long awardId,
            String awardName,
            String awardColor
    ) {
    }
}
