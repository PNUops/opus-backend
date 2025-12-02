package com.opus.opus.modules.team.application;

import com.opus.opus.modules.contest.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.convenience.TeamContestAwardConvenience;
import com.opus.opus.modules.team.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.dto.response.TeamContestAwardResponse.AwardInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamContestAwardQueryService {
    private final TeamConvenience teamConvenience;
    private final TeamContestAwardConvenience teamContestAwardConvenience;
    private final ContestAwardConvenience contestAwardConvenience;

    public TeamContestAwardResponse getTeamAwards(Long teamId) {
        teamConvenience.getTeamById(teamId);

        List<TeamContestAward> teamAwards = teamContestAwardConvenience.findByTeamId(teamId);
        if (teamAwards.isEmpty()) {
            return new TeamContestAwardResponse(List.of());
        }

        List<Long> awardIds = teamAwards.stream()
                .map(TeamContestAward::getContestAwardId)
                .toList();

        List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);

        List<AwardInfo> awardInfos = contestAwards.stream()
                .map(award -> new AwardInfo(
                        award.getId(),
                        award.getAwardName(),
                        award.getAwardColor()
                ))
                .toList();

        return new TeamContestAwardResponse(awardInfos);
    }
}
