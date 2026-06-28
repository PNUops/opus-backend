package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import java.util.List;

public record MentorSubmissionResponse(
        Long submissionId,
        Long submissionItemId,
        String submissionItemName,
        FeedbackStatus feedbackStatus,
        List<ContestSubmissionFileResponse> files
) {
    public static MentorSubmissionResponse of(final ContestSubmission submission, final boolean reviewed,
                                              final List<ContestSubmissionFileResponse> files) {
        return new MentorSubmissionResponse(
                submission.getId(),
                submission.getSubmissionItem().getId(),
                submission.getSubmissionItem().getName(),
                reviewed ? FeedbackStatus.COMPLETED : FeedbackStatus.PENDING,
                files
        );
    }

    public enum FeedbackStatus {
        COMPLETED,
        PENDING
    }
}
