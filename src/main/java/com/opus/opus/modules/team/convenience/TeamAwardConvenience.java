package com.opus.opus.modules.team.convenience;

import com.opus.opus.modules.team.domain.TeamAward;
import com.opus.opus.modules.team.domain.dao.TeamAwardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamAwardConvenience {

    private final TeamAwardRepository teamAwardRepository;

    public void deleteAllByTeamId(Long teamId) {
        teamAwardRepository.deleteAllByTeamId(teamId);
    }

    public void saveAll(List<TeamAward> teamAwards) {
        teamAwardRepository.saveAll(teamAwards);
    }
}
