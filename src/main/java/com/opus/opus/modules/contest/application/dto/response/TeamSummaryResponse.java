package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamAwardResult;
import java.util.List;

public record TeamSummaryResponse(
        Long teamId,
        String teamName,
        String projectName,
        Boolean isLiked,
        Boolean isVoted,
        List<AwardInfo> awards
) {
    public static TeamSummaryResponse of(
            final Team team,
            final List<TeamAwardResult> teamAwardResults,
            final List<ContestAward> contestAwards,
            final Boolean isLiked,
            final Boolean isVoted
    ) {
        final List<AwardInfo> awardInfos = contestAwards.stream()
                .map(AwardInfo::from)
                .toList();

        return new TeamSummaryResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                isLiked,
                isVoted,
                teamAwardResults.stream()
                        .map(result -> new AwardInfo(result.awardName(), result.awardColor()))
                        .toList()
                awardInfos
        );
    }

    public record AwardInfo(
            String awardName,
            String awardColor
    ) {
        public static AwardInfo from(final ContestAward contestAward) {
            return new AwardInfo(
                    contestAward.getAwardName(),
                    contestAward.getAwardColor()
            );
        }
    }
}
