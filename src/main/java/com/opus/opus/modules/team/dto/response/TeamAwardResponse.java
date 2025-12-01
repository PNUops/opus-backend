package com.opus.opus.modules.team.dto.response;

import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record TeamAwardResponse(
        Long teamId,
        List<AwardInfo> awards
) {
    public record AwardInfo(
            Long awardId,
            String awardName,
            String awardColor
    ) {
    }

    public TeamAwardResponse(Team team, List<AwardInfo> awards) {
        this(team.getId(), awards);
    }
}
