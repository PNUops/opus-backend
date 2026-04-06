package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse.AwardInfo;
import com.opus.opus.modules.team.domain.dao.MyProjectFlatResult;
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
    public static MyProjectResponse from(final List<MyProjectFlatResult> results) {
        final MyProjectFlatResult firstResult = results.get(0);
        final List<AwardInfo> awards = results.stream()
                .filter(r -> r.awardName() != null)
                .map(r -> new AwardInfo(r.awardName(), r.awardColor()))
                .toList();
        return new MyProjectResponse(
                firstResult.contestId(), firstResult.contestName(),
                firstResult.teamId(), firstResult.teamName(), firstResult.projectName(),
                firstResult.trackName(), awards
        );
    }
}
