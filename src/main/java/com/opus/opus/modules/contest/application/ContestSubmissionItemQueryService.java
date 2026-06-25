package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionItemQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;

    public ContestSubmissionItemResponse getSubmissionItem(final Long contestId, final Long submissionItemId) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        return ContestSubmissionItemResponse.from(submissionItem);
    }
}
