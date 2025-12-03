package com.opus.opus.modules.team.application;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse.AwardInfo;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamContestAwardQueryService {

    private final TeamConvenience teamConvenience;
    private final ContestAwardConvenience contestAwardConvenience;
    private final TeamContestAwardRepository teamContestAwardRepository;

    public TeamContestAwardResponse getTeamAwards(final Long teamId) {
        teamConvenience.getValidateExistTeam(teamId);

        final List<TeamContestAward> teamAwards = teamContestAwardRepository.findByTeamId(teamId);

        if (teamAwards.isEmpty()) {
            return new TeamContestAwardResponse(List.of());
        }

        final List<Long> awardIds = extractAwardIds(teamAwards);
        final List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);

        return createTeamAwardResponse(contestAwards);
    }

    private List<Long> extractAwardIds(final List<TeamContestAward> teamAwards) {
        return teamAwards.stream()
                .map(TeamContestAward::getContestAwardId)
                .toList();
    }

    private TeamContestAwardResponse createTeamAwardResponse(final List<ContestAward> contestAwards) {
        final List<AwardInfo> awardInfos = contestAwards.stream()
                .map(award -> new AwardInfo(
                        award.getId(),
                        award.getAwardName(),
                        award.getAwardColor()
                ))
                .toList();
        return new TeamContestAwardResponse(awardInfos);
    }
}
