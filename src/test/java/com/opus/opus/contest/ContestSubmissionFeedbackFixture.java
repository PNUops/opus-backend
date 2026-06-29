package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;

public class ContestSubmissionFeedbackFixture {

    public static final String FEEDBACK_DESCRIPTION = "테스트용 피드백입니다.";

    public static ContestSubmissionFeedback createFeedback(final ContestSubmission submission, final Long memberId) {
        return ContestSubmissionFeedback.builder()
                .description(FEEDBACK_DESCRIPTION)
                .memberId(memberId)
                .submission(submission)
                .build();
    }

    public static ContestSubmissionFeedback createReadFeedback(final ContestSubmission submission, final Long memberId) {
        final ContestSubmissionFeedback feedback = ContestSubmissionFeedback.builder()
                .description(FEEDBACK_DESCRIPTION)
                .memberId(memberId)
                .submission(submission)
                .build();
        feedback.markAsRead();
        return feedback;
    }
}
