package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.AWARD_NOT_IN_TEAM_CONTEST;
import static com.opus.opus.modules.team.exception.TeamAwardExceptionType.DUPLICATE_AWARD_IDS;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.request.TeamContestAwardUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
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
    private final ContestAwardConvenience contestAwardConvenience;
    private final TeamContestAwardRepository teamContestAwardRepository;

    public TeamContestAwardResponse updateTeamContestAwards(final Long teamId,
                                                            final TeamContestAwardUpdateRequest request) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);

        final List<Long> awardIds = request.awardIds();
        validateDuplicateAwardIds(awardIds);

        deleteExistingTeamAwards(teamId);
        if (awardIds.isEmpty()) {
            return new TeamContestAwardResponse(List.of());
        }

        final List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);
        validateTeamContestAwardsBelonging(contestAwards, team.getContestId());

        saveTeamContestAwards(team, contestAwards);
        return TeamContestAwardResponse.from(contestAwards);
    }

    public void deleteExistingTeamAwards(final Long teamId) {
        final List<TeamContestAward> existingAwards = teamContestAwardRepository.findByTeamId(teamId);
        teamContestAwardRepository.deleteAll(existingAwards);
    }

    private void validateDuplicateAwardIds(final List<Long> awardIds) {
        final Set<Long> uniqueIds = new HashSet<>(awardIds);
        if (uniqueIds.size() != awardIds.size()) {
            throw new TeamAwardException(DUPLICATE_AWARD_IDS);
        }
    }

    private void validateTeamContestAwardsBelonging(final List<ContestAward> contestAwards, final Long teamContestId) {
        final boolean hasInvalidContestAward = contestAwards.stream()
                .anyMatch(award -> !award.getContest().getId().equals(teamContestId));
        if (hasInvalidContestAward) {
            throw new TeamAwardException(AWARD_NOT_IN_TEAM_CONTEST);
        }
    }

    private void saveTeamContestAwards(final Team team, final List<ContestAward> contestAwards) {
        final List<TeamContestAward> teamAwards = contestAwards.stream()
                .map(award -> TeamContestAward.builder()
                        .team(team)
                        .contestAwardId(award.getId())
                        .build())
                .toList();
        teamContestAwardRepository.saveAll(teamAwards);
    }
}
