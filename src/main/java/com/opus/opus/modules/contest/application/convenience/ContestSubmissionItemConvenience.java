package com.opus.opus.modules.contest.application.convenience;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionItemConvenience {

    private final ContestSubmissionItemRepository contestSubmissionItemRepository;

    public ContestSubmissionItem getValidateExistSubmissionItem(final Long submissionItemId) {
        return contestSubmissionItemRepository.findById(submissionItemId)
                .orElseThrow(() -> new ContestException(ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM));
    }

    public ContestSubmissionItem getValidateExistSubmissionItem(final Long contestId, final Long submissionItemId) {
        final ContestSubmissionItem submissionItem = contestSubmissionItemRepository.findById(submissionItemId)
                .orElseThrow(() -> new ContestSubmissionItemException(ContestSubmissionItemExceptionType.NOT_FOUND_SUBMISSION_ITEM));

        if (!submissionItem.getContest().getId().equals(contestId)) {
            throw new ContestSubmissionItemException(ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST);
        }
        return submissionItem;
    }
}
