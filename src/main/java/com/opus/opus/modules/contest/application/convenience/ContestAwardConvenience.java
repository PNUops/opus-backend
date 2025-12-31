package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestAwardConvenience {

    private final ContestAwardRepository contestAwardRepository;
    private final TeamContestAwardRepository teamContestAwardRepository;

    public List<ContestAward> findAllById(final List<Long> awardIds) {
        final List<ContestAward> contestAwards = contestAwardRepository.findAllById(awardIds);

        if (contestAwards.size() != awardIds.size()) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        return contestAwards;
    }

    public List<ContestAward> getTeamAwards(final List<Team> teams) {
        final List<Long> teamIds = teams.stream().map(Team::getId).toList();

        final List<TeamContestAward> teamAwards = teamContestAwardRepository.findByTeamIdIn(teamIds);

        if (teamAwards.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Long> awardIds = teamAwards.stream().map(TeamContestAward::getContestAwardId).distinct().toList();

        return findAllById(awardIds);
    }
}
