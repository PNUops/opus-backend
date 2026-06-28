package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionStatus;
import java.time.LocalDateTime;

public record ContestSubmissionTimelineResponse(
        SubmissionStatus status,
        LocalDateTime deadlineAt,
        String submissionItemName
) {
    public static ContestSubmissionTimelineResponse from(final ContestSubmission submission) {
        final ContestSubmissionItem item = submission.getSubmissionItem();
        return new ContestSubmissionTimelineResponse(
                submission.isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED,
                item.getEndAt(),
                item.getName()
        );
    }
}
