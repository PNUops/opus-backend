package com.opus.opus.modules.contest.application.convenience;

import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestMemberConvenience {

    private final ContestMemberRepository contestMemberRepository;

    public boolean isAssignedTeam(final Long contestId, final Long memberId, final Long teamId) {
        return contestMemberRepository.findByContestIdAndMemberId(contestId, memberId)
                .map(contestMember -> contestMember.getTeamIds().contains(teamId))
                .orElse(false);
    }
}
