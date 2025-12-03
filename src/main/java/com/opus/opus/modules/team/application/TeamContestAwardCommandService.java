package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.AWARD_NOT_IN_TEAM_CONTEST;
import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.DUPLICATE_AWARD_IDS;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.application.convenience.TeamContestAwardConvenience;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.application.dto.request.TeamContestAwardUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse.AwardInfo;
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
public class TeamContestAwardCommandService {

    private final TeamConvenience teamConvenience;
    private final TeamContestAwardConvenience teamContestAwardConvenience;
    private final ContestAwardConvenience contestAwardConvenience;

    public TeamContestAwardResponse updateTeamAwards(Long teamId, TeamContestAwardUpdateRequest request) {
        Team team = teamConvenience.getValidateExistTeam(teamId);

        List<Long> awardIds = request.awardIds();
        validateDuplicate(awardIds);

        teamContestAwardConvenience.deleteAllByTeamId(teamId);

        if (awardIds.isEmpty()) {
            return new TeamContestAwardResponse(List.of());
        }

        List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);
        validateContestAwards(contestAwards, awardIds, team.getContestId());

        List<TeamContestAward> teamAwards = contestAwards.stream()
                .map(award -> TeamContestAward.builder()
                        .team(team)
                        .contestAwardId(award.getId())
                        .build())
                .toList();
        teamContestAwardConvenience.saveAll(teamAwards);

        List<AwardInfo> awardInfos = contestAwards.stream()
                .map(award -> new AwardInfo(
                        award.getId(),
                        award.getAwardName(),
                        award.getAwardColor()
                ))
                .toList();
        return new TeamContestAwardResponse(awardInfos);
    }

    private void validateDuplicate(List<Long> awardIds) {
        Set<Long> uniqueIds = new HashSet<>(awardIds);
        if (uniqueIds.size() != awardIds.size()) {
            throw new TeamAwardException(DUPLICATE_AWARD_IDS);
        }
    }

    private void validateContestAwards(List<ContestAward> contestAwards, List<Long> awardIds, Long teamContestId) {
        boolean hasInvalidContestAward = contestAwards.stream()
                .anyMatch(award -> !award.getContest().getId().equals(teamContestId));
        if (hasInvalidContestAward) {
            throw new TeamAwardException(AWARD_NOT_IN_TEAM_CONTEST);
        }
    }
}
