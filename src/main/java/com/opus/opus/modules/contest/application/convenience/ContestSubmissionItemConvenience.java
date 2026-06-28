package com.opus.opus.modules.contest.application.convenience;


import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.exception.ContestException;
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
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION_ITEM));
    }
}
