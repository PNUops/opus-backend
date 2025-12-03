package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamContestAwardConvenience {

    private final TeamContestAwardRepository teamContestAwardRepository;

    public void deleteAllByTeamId(Long teamId) {
        List<TeamContestAward> existingAwards = teamContestAwardRepository.findByTeamId(teamId);
        teamContestAwardRepository.deleteAll(existingAwards);
    }

    public void saveAll(List<TeamContestAward> teamAwards) {
        teamContestAwardRepository.saveAll(teamAwards);
    }

    public List<TeamContestAward> findByTeamId(Long teamId) {
        return teamContestAwardRepository.findByTeamId(teamId);
    }
}
