package com.opus.opus.modules.team.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestAward;
import java.util.List;

public record TeamContestAwardResponse(
        List<AwardInfo> awards
) {

    public static TeamContestAwardResponse from(final List<ContestAward> contestAwards) {
        final List<AwardInfo> awardInfos = contestAwards.stream()
                .map(AwardInfo::from)
                .toList();
        return new TeamContestAwardResponse(awardInfos);
    }

    public record AwardInfo(
            Long awardId,
            String awardName,
            String awardColor
    ) {
        public static AwardInfo from(final ContestAward contestAward) {
            return new AwardInfo(
                    contestAward.getId(),
                    contestAward.getAwardName(),
                    contestAward.getAwardColor()
            );
        }
    }
}
