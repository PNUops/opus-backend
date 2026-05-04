package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamLikeConvenience {

    private final TeamLikeRepository teamLikeRepository;

    public long countAllLikes() {
        return teamLikeRepository.countByTeamIsDeletedFalse();
    }

    // 1. 대회 내 좋아요한 팀 ID 집합 조회
    public Set<Long> getLikedTeamIdsIfInPeriod(final Long contestId, final Member member,
                                               final boolean isVotingPeriod) {
        return (member != null && !isVotingPeriod)
                ? getLikedTeamIdsByContestId(contestId, member)
                : Set.of();
    }

    private Set<Long> getLikedTeamIdsByContestId(final Long contestId, final Member member) {
        return teamLikeRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .map(tl -> tl.getTeam().getId())
                .collect(toUnmodifiableSet());
    }

    // 2. 단일 팀 좋아요 여부 조회
    public boolean getIsLikedIfInPeriod(final Team team, final Member member, final boolean isVotingPeriod) {
        return member != null && !isVotingPeriod && getIsLikedByTeamAndMember(team, member);
    }

    private boolean getIsLikedByTeamAndMember(final Team team, final Member member) {
        return teamLikeRepository.existsByMemberIdAndTeam(member.getId(), team);
    }
}
