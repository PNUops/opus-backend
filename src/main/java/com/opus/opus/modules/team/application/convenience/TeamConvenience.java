package com.opus.opus.modules.team.application.convenience;


import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST_SORT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.CONTEST_HAS_TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TRACK_HAS_TEAM;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.domain.SortType;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRankingResult;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamConvenience {

    private final TeamRepository teamRepository;

    public Team getValidateExistTeam(final Long teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new TeamException(NOT_FOUND_TEAM));
    }

    public void validateExistTeam(final Long teamId) {
        teamRepository.findById(teamId).orElseThrow(() -> new TeamException(NOT_FOUND_TEAM));
    }

    public void validateAllTeamsDeletedInContest(final Long contestId) {
        if (teamRepository.existsByContestId(contestId)) {
            throw new TeamException(CONTEST_HAS_TEAM);
        }
    }

    public void validateAllTeamsDeletedInTrack(final Long trackId) {
        if (teamRepository.existsByTrackId(trackId)) {
            throw new TeamException(TRACK_HAS_TEAM);
        }
    }

    public List<Team> getTeamsOfContest(final Long contestId) {
        return teamRepository.findAllByContestId(contestId);
    }

    public Map<Long, Team> getTeamsByIds(final List<Long> teamIds) {
        return teamRepository.findAllById(teamIds).stream()
                .collect(toMap(
                        Team::getId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    public void validateTeamsInContest(final Long contestId, final List<Long> teamIds) {
        final Map<Long, Team> teams = getTeamsByIds(teamIds);
        teamIds.forEach(teamId -> validateTeamInContest(contestId, teams.get(teamId)));
    }


    public Team getValidateTeamInContest(final Long teamId, final Long contestId) {
        final Team team = getValidateExistTeam(teamId);
        if (!team.getContestId().equals(contestId)) {
            throw new TeamException(TEAM_NOT_IN_CONTEST);
        }
        return team;
    }

    private void validateTeamInContest(final Long contestId, final Team team) {
        if (team == null) {
            throw new TeamException(NOT_FOUND_TEAM);
        }
        if (!team.getContestId().equals(contestId)) {
            throw new TeamException(TEAM_NOT_IN_CONTEST);
        }
    }

    public Team save(final Team team) {
        return teamRepository.save(team);
    }

    public List<TeamRankingResult> getTeamRankingResults(final Long contestId) {
        return teamRepository.findTeamRankingByContestId(contestId);
    }

    public long countSubmittedTeams() {
        return teamRepository.countByIsSubmittedTrue();
    }

    public void sortTeams(final List<Team> teams, final SortType mode, final Member member) {
        if (teams == null || teams.isEmpty()) {
            return;
        }

        switch (mode) {
            case RANDOM -> shuffleTeams(teams, member);
            case ASC -> teams.sort(Comparator.comparing(Team::getId));
            case CUSTOM -> teams.sort(Comparator.comparing(Team::getItemOrder));
            default -> throw new ContestException(NOT_FOUND_CONTEST_SORT);
        }
    }

    public void shuffleTeams(final List<Team> teams, final Member member) {
        if (member != null) {
            Random seed = new Random(member.getId());
            Collections.shuffle(teams, seed);
        } else {
            Collections.shuffle(teams);
        }
    }
}
