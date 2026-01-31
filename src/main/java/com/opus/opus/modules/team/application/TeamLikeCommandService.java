package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_LIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_UNLIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.DUPLICATE_LIKE_REQUEST;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.NOT_LIKED_YET;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.exception.TeamLikeException;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamLikeCommandService {

    private final TeamConvenience teamConvenience;
    private final ContestConvenience contestConvenience;
    private final TeamLikeRepository teamLikeRepository;

    public TeamLikeToggleResponse toggleLike(Long memberId, Long teamId, Boolean isLiked) {
        Team team = teamConvenience.getValidateExistTeam(teamId);
        Contest contest = contestConvenience.getValidateExistContest(team.getContestId());

        contestConvenience.validateNotInVotingPeriod(contest);

        Optional<TeamLike> teamLikeOptional = teamLikeRepository.findByMemberIdAndTeam(memberId, team);
        return teamLikeOptional
                .map(teamLike -> handleExistingLike(teamLike, isLiked))
                .orElseGet(() -> handleFirstTimeLike(memberId, team, isLiked));
    }

    private TeamLikeToggleResponse handleFirstTimeLike(Long memberId, Team team, Boolean isLiked) {
        if (!isLiked) {
            throw new TeamLikeException(NOT_LIKED_YET);
        }

        saveTeamLike(memberId, team, true);
        return TeamLikeToggleResponse.of(team.getId(), true, "좋아요가 등록되었습니다.");
    }

    private TeamLikeToggleResponse handleExistingLike(final TeamLike teamLike, final Boolean isLiked) {
        if (Objects.equals(teamLike.getIsLiked(), isLiked)) {
            throw new TeamLikeException(isLiked ? ALREADY_LIKED : ALREADY_UNLIKED);
        }

        teamLike.updateIsLiked(isLiked);

        return TeamLikeToggleResponse.of(teamLike.getTeam().getId(), isLiked, isLiked ? "좋아요가 등록되었습니다." : "좋아요가 취소되었습니다.");
    }

    private void saveTeamLike(Long memberId, Team team, Boolean isLiked) {
        try {
            teamLikeRepository.save(TeamLike.builder()
                    .memberId(memberId)
                    .team(team)
                    .isLiked(isLiked)
                    .build());
            teamLikeRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new TeamLikeException(DUPLICATE_LIKE_REQUEST);
        }
    }
}
