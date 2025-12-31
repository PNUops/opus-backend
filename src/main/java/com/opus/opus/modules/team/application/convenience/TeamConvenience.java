package com.opus.opus.modules.team.application.convenience;


import static com.opus.opus.modules.team.exception.TeamExceptionType.CONTEST_HAS_TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TRACK_HAS_TEAM;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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

    public List<Team> findAllByContestId(final Long contestId) {
        return teamRepository.findByContestId(contestId);
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
