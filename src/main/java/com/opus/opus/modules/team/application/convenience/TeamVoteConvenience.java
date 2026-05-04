package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import java.util.List;
import java.util.Set;
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


    // 1. 대회 내 투표한 팀 ID 집합 조회
    public Set<Long> getVotedTeamIdsIfInPeriod(final Long contestId, final Member member,
                                               final boolean isVotingPeriod) {
        return (member != null && isVotingPeriod)
                ? getVotedTeamIdsByContestId(contestId, member)
                : Set.of();
    }

    private Set<Long> getVotedTeamIdsByContestId(final Long contestId, final Member member) {
        return teamVoteRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .map(tv -> tv.getTeam().getId())
                .collect(toUnmodifiableSet());
    }

    // 2. 단일 팀 투표 여부 조회
    public boolean getIsVotedIfInPeriod(final Team team, final Member member, final boolean isVotingPeriod) {
        return member != null && isVotingPeriod && getIsVotedByTeamAndMember(team, member);
    }

    private boolean getIsVotedByTeamAndMember(final Team team, final Member member) {
        return teamVoteRepository.existsByMemberIdAndTeam(member.getId(), team);
    }
}
