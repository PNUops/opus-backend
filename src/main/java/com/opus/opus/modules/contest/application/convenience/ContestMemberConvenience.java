package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_FOUND_CONTEST_MEMBER;

import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import java.util.List;
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

    public List<ContestMember> getAssignedContestMembers(final Long memberId) {
        return contestMemberRepository.findAllByMemberId(memberId);
    }

    public ContestMember getValidateExistContestMember(final Long contestId, final Long memberId) {
        return contestMemberRepository.findByContestIdAndMemberId(contestId, memberId)
                .orElseThrow(() -> new ContestMemberException(NOT_FOUND_CONTEST_MEMBER));
    }
}
