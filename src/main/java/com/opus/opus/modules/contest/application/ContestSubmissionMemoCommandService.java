package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.MEMO_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;

import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionMemoRequest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
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
@Transactional
public class ContestSubmissionMemoCommandService {

    private final ContestSubmissionConvenience submissionConvenience;
    private final ContestSubmissionMemoRepository memoRepository;
    private final TeamMemberConvenience teamMemberConvenience;

    public void createMemo(final Long contestId, final Long teamId, final Long submissionId,
                           final ContestSubmissionMemoRequest request, final Member member) {
        final ContestSubmission submission = submissionConvenience.getValidatedSubmission(contestId, teamId, submissionId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        if (memoRepository.existsBySubmissionId(submissionId)) {
            throw new ContestSubmissionMemoException(MEMO_ALREADY_EXISTS);
        }

        memoRepository.save(ContestSubmissionMemo.builder()
                .content(request.content())
                .submission(submission)
                .build());
    }

    public void updateMemo(final Long contestId, final Long teamId, final Long submissionId,
                           final ContestSubmissionMemoRequest request, final Member member) {
        submissionConvenience.getValidatedSubmission(contestId, teamId, submissionId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ContestSubmissionMemoException(NOT_FOUND_MEMO));

        memo.updateContent(request.content());
    }

    public void deleteMemo(final Long contestId, final Long teamId, final Long submissionId,
                           final Member member) {
        submissionConvenience.getValidatedSubmission(contestId, teamId, submissionId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ContestSubmissionMemoException(NOT_FOUND_MEMO));

        memoRepository.delete(memo);
    }
}
