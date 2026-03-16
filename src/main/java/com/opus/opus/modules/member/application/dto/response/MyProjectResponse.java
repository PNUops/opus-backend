package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse.AwardInfo;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record MyProjectResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        List<AwardInfo> awards
) {
    public static MyProjectResponse of(final Team team, final String contestName, final String trackName, final List<ContestAward> awards) {
        return new MyProjectResponse(
                team.getContestId(),
                contestName,
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                trackName,
                awards.stream().map(AwardInfo::from).toList()
        );
    }
}
