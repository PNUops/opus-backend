package com.opus.opus.modules.team.convenience;

import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamAwardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamAwardConvenience {

    private final TeamAwardRepository teamAwardRepository;

    public void deleteAllByTeamId(Long teamId) {
        List<TeamContestAward> existingAwards = teamAwardRepository.findByTeamId(teamId);
        teamAwardRepository.deleteAll(existingAwards);
    }

    public void saveAll(List<TeamContestAward> teamAwards) {
        teamAwardRepository.saveAll(teamAwards);
    }
}
