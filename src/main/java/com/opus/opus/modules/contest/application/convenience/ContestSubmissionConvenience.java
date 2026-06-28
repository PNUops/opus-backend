package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionConvenience {

    private final ContestSubmissionRepository contestSubmissionRepository;

    public ContestSubmission getValidateExistSubmission(final Long submissionId) {
        return contestSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION));
    }

    public ContestSubmission getValidateSubmissionInContest(final Long contestId, final Long submissionId) {
        final ContestSubmission submission = getValidateExistSubmission(submissionId);
        if (!submission.isInContest(contestId)) {
            throw new ContestException(INVALID_SUBMISSION_FOR_CONTEST);
        }
        return submission;
    }

    public boolean isSubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        return contestSubmissionRepository.existsByTeamIdAndSubmissionItem(teamId, submissionItem);
    }
}
