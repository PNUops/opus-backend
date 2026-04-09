package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.projection.TeamAwardProjection;
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
            final List<TeamAwardProjection> teamAwardResults,
            final Boolean isLiked,
            final Boolean isVoted
    ) {
        return new TeamSummaryResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                isLiked,
                isVoted,
                teamAwardResults.stream()
                        .map(result -> new AwardInfo(result.getAwardName(), result.getAwardColor()))
                        .toList()
        );
    }

    public record AwardInfo(
            String awardName,
            String awardColor
    ) {
    }
}
