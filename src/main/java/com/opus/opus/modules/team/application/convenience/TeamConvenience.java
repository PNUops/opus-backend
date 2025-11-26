package com.opus.opus.modules.team.application.convenience;

import static com.opus.opus.modules.team.exception.TeamExceptionType.CONTEST_HAS_TEAM;

import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamConvenience {
    private final TeamRepository teamRepository;

    public void validateAllTeamsDeleted(final Long contestId) {
        if (teamRepository.existsByContestId(contestId)) {
            throw new TeamException(CONTEST_HAS_TEAM);
        }
    }
}
