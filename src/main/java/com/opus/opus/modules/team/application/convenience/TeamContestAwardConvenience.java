package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamContestAwardConvenience {

    private final TeamContestAwardRepository teamContestAwardRepository;

    private final ContestAwardConvenience contestAwardConvenience;

    public List<ContestAward> getTeamAwards(final List<Team> teams) {
        final List<Long> teamIds = teams.stream().map(Team::getId).toList();

        final List<TeamContestAward> teamAwards = teamContestAwardRepository.findByTeamIdIn(teamIds);

        if (teamAwards.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> awardIds = teamAwards.stream().map(TeamContestAward::getContestAwardId).distinct().toList();

        return contestAwardConvenience.findAllById(awardIds);
    }
}
