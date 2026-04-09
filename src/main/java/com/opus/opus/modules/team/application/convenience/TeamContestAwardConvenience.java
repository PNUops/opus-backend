package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.projection.TeamAwardProjection;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamContestAwardConvenience {

    private final TeamContestAwardRepository teamContestAwardRepository;

    public List<TeamAwardProjection> findTeamAwardsByTeams(final List<Team> teams) {
        final List<Long> teamIds = teams.stream().map(Team::getId).toList();

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        return teamContestAwardRepository.findTeamAwardsByTeamIds(teamIds);
    }

    public Map<Long, List<TeamAwardProjection>> getTeamAwardProjectionMap(final List<Team> teams) {
        final List<TeamAwardProjection> teamAwardResults = findTeamAwardsByTeams(teams);

        return teamAwardResults.stream()
                .collect(Collectors.groupingBy(TeamAwardProjection::getTeamId));
    }
}
