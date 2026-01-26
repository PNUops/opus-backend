package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_UNVOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_VOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.VOTE_LIMIT_EXCEEDED;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamVoteException;
import com.opus.opus.modules.team.exception.TeamVoteExceptionType;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamVoteCommandService {

    private final TeamConvenience teamConvenience;
    private final ContestConvenience contestConvenience;

    private final TeamVoteRepository teamVoteRepository;

    public TeamVoteToggleResponse toggleVote(Long memberId, Long teamId, Boolean isVoted) {
        Team team = teamConvenience.getValidateExistTeam(teamId);
        Contest contest = contestConvenience.getValidateExistContest(team.getContestId());

        contestConvenience.validateVotingPeriod(team.getContestId());

        Optional<TeamVote> teamVoteOptional = teamVoteRepository.findByMemberIdAndTeam(memberId, team);

        return teamVoteOptional.map(teamVote -> handleExistingVote(teamVote, isVoted, memberId, contest))
                .orElseGet(() -> handleFirstTimeVote(memberId, team, isVoted, contest));
    }

    private TeamVoteToggleResponse handleFirstTimeVote(Long memberId, Team team, Boolean isVoted, Contest contest) {
        long currentVoteCount = countCurrentMemberVotes(memberId, team.getContestId());
        int maxVotesLimit = contest.getMaxVotesLimit();

        if (isVoted) {
            validateVoteLimit(currentVoteCount, maxVotesLimit);
            currentVoteCount++;
        }

        saveTeamVote(memberId, team, isVoted);

        String message = isVoted ? "투표가 처음 등록되었습니다." : "투표가 비활성화된 상태로 초기화되었습니다.";
        return TeamVoteToggleResponse.of(team.getId(), isVoted, message, currentVoteCount, maxVotesLimit);
    }

    private TeamVoteToggleResponse handleExistingVote(TeamVote teamVote, Boolean isVoted, Long memberId, Contest contest) {
        if (Objects.equals(teamVote.getIsVoted(), isVoted)) {
            TeamVoteExceptionType exceptionType = isVoted ? ALREADY_VOTED : ALREADY_UNVOTED;
            throw new TeamVoteException(exceptionType);
        }

        long currentVoteCount = countCurrentMemberVotes(memberId, contest.getId());
        int maxVotesLimit = contest.getMaxVotesLimit();

        if (isVoted) {
            validateVoteLimit(currentVoteCount, maxVotesLimit);
            currentVoteCount++;
        } else {
            currentVoteCount--;
        }

        teamVote.updateIsVoted(isVoted);

        String message = isVoted ? "투표가 등록되었습니다." : "투표가 취소되었습니다.";
        return TeamVoteToggleResponse.of(teamVote.getTeam().getId(), isVoted, message, currentVoteCount, maxVotesLimit);
    }

    private long countCurrentMemberVotes(Long memberId, Long contestId) {
        return teamVoteRepository.countMemberVotesInContest(memberId, contestId);
    }

    private void validateVoteLimit(long currentVoteCount, int maxVotesLimit) {
        if (currentVoteCount >= maxVotesLimit) {
            String message = String.format(VOTE_LIMIT_EXCEEDED.errorMessage(), maxVotesLimit);
            throw new TeamVoteException(VOTE_LIMIT_EXCEEDED, message);
        }
    }

    private void saveTeamVote(Long memberId, Team team, Boolean isVoted) {
        teamVoteRepository.save(TeamVote.builder()
                .memberId(memberId)
                .team(team)
                .isVoted(isVoted)
                .build());
    }

    @Transactional(readOnly = true)
    public MemberVoteCountResponse getMemberVoteCount(Long memberId, Long contestId) {
        Contest contest = contestConvenience.getValidateExistContest(contestId);
        long currentVoteCount = teamVoteRepository.countMemberVotesInContest(memberId, contestId);
        long remainingVotesCount = contest.getMaxVotesLimit() - currentVoteCount;
        return new MemberVoteCountResponse(remainingVotesCount, (long) contest.getMaxVotesLimit());
    }
}
