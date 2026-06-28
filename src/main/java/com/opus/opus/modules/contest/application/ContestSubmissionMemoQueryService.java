package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;

import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMemoResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionMemoRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionMemoQueryService {

    private final ContestSubmissionConvenience submissionConvenience;
    private final ContestSubmissionMemoRepository memoRepository;
    private final TeamMemberConvenience teamMemberConvenience;

    public ContestSubmissionMemoResponse getMemo(final Long contestId, final Long teamId,
                                                 final Long submissionId, final Member member) {
        submissionConvenience.getValidatedSubmission(contestId, teamId, submissionId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ContestSubmissionMemoException(NOT_FOUND_MEMO));

        return ContestSubmissionMemoResponse.from(memo);
    }
}
