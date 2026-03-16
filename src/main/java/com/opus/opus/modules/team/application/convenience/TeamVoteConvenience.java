package com.opus.opus.modules.team.application.convenience;

import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamVoteConvenience {

    private final TeamVoteRepository teamVoteRepository;

    public Page<TeamVote> getAllTeamVoteDesc(final List<Long> teamIds, final Pageable pageable) {
        return teamVoteRepository.findByTeamIdInOrderByCreatedAtDesc(teamIds, pageable);
    }

    public VoteStatisticsResult getVoteStaticsResult(final Long contestId) {
        return teamVoteRepository.countVoteStatisticsByContest(contestId);
    }

    public long countMemberVotesInContest(final Long memberId, final Long contestId) {
        return teamVoteRepository.countMemberVotesInContest(memberId, contestId);
    }

    public List<TeamVote> getCurrentVotes(final Long memberId) {
        return teamVoteRepository.findAllByMemberIdAndIsVotedTrueOrderByCreatedAtDesc(memberId);
    }
}
