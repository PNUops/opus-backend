package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import java.util.List;
import java.util.Map;
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

    public Map<Long, Boolean> getVoteMap(final Long contestId, final Member member) {
        return teamVoteRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .collect(toMap(tv -> tv.getTeam().getId(), TeamVote::getIsVoted));
    }
}
