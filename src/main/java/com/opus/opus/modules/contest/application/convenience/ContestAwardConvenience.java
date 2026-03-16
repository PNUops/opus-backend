package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public Map<Long, List<ContestAward>> getAwardsByTeamIds(final List<Long> teamIds) {
        final List<TeamContestAward> teamAwards = teamContestAwardRepository.findByTeamIdIn(teamIds);
        final Map<Long, ContestAward> awardMap = buildAwardMap(teamAwards);

        return groupAwardsByTeamId(teamAwards, awardMap);
    }

    private Map<Long, ContestAward> buildAwardMap(final List<TeamContestAward> teamAwards) {
        final List<Long> awardIds = teamAwards.stream()
                .map(TeamContestAward::getContestAwardId)
                .distinct()
                .toList();

        return contestAwardRepository.findAllById(awardIds).stream()
                .collect(Collectors.toMap(ContestAward::getId, award -> award));
    }

    private Map<Long, List<ContestAward>> groupAwardsByTeamId(final List<TeamContestAward> teamAwards, final Map<Long, ContestAward> awardMap) {
        final Map<Long, List<ContestAward>> result = new HashMap<>();
        for (final TeamContestAward ta : teamAwards) {
            final ContestAward award = awardMap.get(ta.getContestAwardId());
            if (award != null) {
                result.computeIfAbsent(ta.getTeam().getId(), k -> new ArrayList<>()).add(award);
            }
        }
        return result;
    }
}
