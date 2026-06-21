package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
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
                .orElseThrow(() -> new ContestSubmissionException(NOT_FOUND_SUBMISSION));
    }

    public void validateExistSubmission(final Long submissionId) {
        if (!contestSubmissionRepository.existsById(submissionId)) {
            throw new ContestSubmissionException(NOT_FOUND_SUBMISSION);
        }
    }
}
