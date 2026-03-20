package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamAwardResult;
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

    public List<TeamAwardResult> getTeamAwardResult(final Long teamId) {
        return teamContestAwardRepository.findTeamAwardsByTeamId(teamId);
    }

    public List<TeamAwardResult> findTeamAwardsByTeams(final List<Team> teams) {
        final List<Long> teamIds = teams.stream().map(Team::getId).toList();

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        return teamContestAwardRepository.findTeamAwardsByTeamIds(teamIds);
    }

    public Map<Long, List<TeamAwardResult>> getTeamAwardResultMap(final List<Team> teams) {
        final List<TeamAwardResult> teamAwardResults = findTeamAwardsByTeams(teams);

        return teamAwardResults.stream()
                .collect(Collectors.groupingBy(TeamAwardResult::teamId));
    }
}
