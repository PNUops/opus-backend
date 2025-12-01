package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;
import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.AWARD_NOT_IN_TEAM_CONTEST;
import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.DUPLICATE_AWARD_IDS;

import com.opus.opus.modules.contest.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import com.opus.opus.modules.team.convenience.TeamAwardConvenience;
import com.opus.opus.modules.team.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.dto.request.TeamAwardUpdateRequest;
import com.opus.opus.modules.team.dto.response.TeamAwardResponse;
import com.opus.opus.modules.team.dto.response.TeamAwardResponse.AwardInfo;
import com.opus.opus.modules.team.exception.TeamAwardException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamAwardCommandService {

    private final TeamConvenience teamConvenience;
    private final TeamAwardConvenience teamAwardConvenience;
    private final ContestAwardConvenience contestAwardConvenience;

    public TeamAwardResponse updateTeamAwards(Long teamId, TeamAwardUpdateRequest request) {
        Team team = teamConvenience.getTeamById(teamId);

        List<Long> awardIds = request.awardIds();
        validateNoDuplicates(awardIds);

        teamAwardConvenience.deleteAllByTeamId(teamId);

        if (awardIds.isEmpty()) {
            return new TeamAwardResponse(team, List.of());
        }

        List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);
        validateContestAwards(contestAwards, awardIds, team.getContestId());

        List<TeamContestAward> teamAwards = contestAwards.stream()
                .map(award -> TeamContestAward.builder()
                        .team(team)
                        .contestAwardId(award.getId())
                        .build())
                .toList();
        teamAwardConvenience.saveAll(teamAwards);

        List<AwardInfo> awardInfos = contestAwards.stream()
                .map(award -> new AwardInfo(
                        award.getId(),
                        award.getAwardName(),
                        award.getAwardColor()
                ))
                .toList();

        return new TeamAwardResponse(team, awardInfos);
    }

    private void validateNoDuplicates(List<Long> awardIds) {
        Set<Long> uniqueIds = new HashSet<>(awardIds);
        if (uniqueIds.size() != awardIds.size()) {
            throw new TeamAwardException(DUPLICATE_AWARD_IDS);
        }
    }

    private void validateContestAwards(List<ContestAward> contestAwards, List<Long> awardIds, Long teamContestId) {
        if (contestAwards.size() != awardIds.size()) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        boolean hasInvalidContestAward = contestAwards.stream()
                .anyMatch(award -> !award.getContest().getId().equals(teamContestId));

        if (hasInvalidContestAward) {
            throw new TeamAwardException(AWARD_NOT_IN_TEAM_CONTEST);
        }
    }
}
