package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse.AwardInfo;
import com.opus.opus.modules.team.domain.dao.MyProjectFlatResult;
import java.util.List;
import java.util.stream.Collectors;

public record MyProjectResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        List<AwardInfo> awards
) {
    public static List<MyProjectResponse> fromFlatResults(final List<MyProjectFlatResult> results) {
        return results.stream()
                .collect(Collectors.groupingBy(MyProjectFlatResult::teamId))
                .values().stream()
                .map(group -> {
                    final MyProjectFlatResult first = group.get(0);
                    final List<AwardInfo> awards = group.stream()
                            .filter(r -> r.awardName() != null)
                            .map(r -> new AwardInfo(r.awardName(), r.awardColor()))
                            .toList();
                    return new MyProjectResponse(
                            first.contestId(),
                            first.contestName(),
                            first.teamId(),
                            first.teamName(),
                            first.projectName(),
                            first.trackName(),
                            awards
                    );
                })
                .toList();
    }
}
