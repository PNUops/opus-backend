package com.opus.opus.modules.team.convenience;


import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamConvenience {
    private final TeamRepository teamRepository;

    public Team getTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamException(NOT_FOUND_TEAM));
    }
}
