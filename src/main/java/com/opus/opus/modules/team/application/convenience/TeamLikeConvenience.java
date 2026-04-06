package com.opus.opus.modules.team.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamLikeConvenience {

    private final TeamLikeRepository teamLikeRepository;

    public long countAllLikes() {
        return teamLikeRepository.countByIsLikedTrueAndTeamIsDeletedFalse();
    }

    public Map<Long, Boolean> getLikeMap(final Long contestId, final Member member) {
        return teamLikeRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .collect(toMap(tl -> tl.getTeam().getId(), TeamLike::getIsLiked));
    }

    // 1. 대회 내 모든 팀 맵 조회
    public Map<Long, Boolean> getLikeMapIfInPeriod(final Long contestId, final Member member,
                                                   final boolean isVotingPeriod) {
        return (member != null && !isVotingPeriod)
                ? getLikeMapByContestId(contestId, member)
                : Map.of();
    }

    private Map<Long, Boolean> getLikeMapByContestId(final Long contestId, final Member member) {
        return teamLikeRepository.findAllByMemberIdAndContestId(member.getId(), contestId).stream()
                .collect(toMap(
                        tv -> tv.getTeam().getId(),
                        TeamLike::getIsLiked
                ));
    }

    // 2. 단일 팀 투표 여부 조회
    public boolean getIsLikedIfInPeriod(final Team team, final Member member, final boolean isVotingPeriod) {
        return member != null && !isVotingPeriod && getIsLikedByTeamAndMember(team, member);
    }

    private boolean getIsLikedByTeamAndMember(final Team team, final Member member) {
        return teamLikeRepository.findByMemberIdAndTeam(member.getId(), team)
                .map(TeamLike::getIsLiked)
                .orElse(false);
    }
}
