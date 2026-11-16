package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;

public class ContestSubmissionFeedbackFixture {

    public static final String FEEDBACK_DESCRIPTION = "테스트용 피드백입니다.";

    public static ContestSubmissionFeedback createFeedback(final ContestSubmissionItem submissionItem,
                                                           final Long teamId, final Long memberId) {
        return ContestSubmissionFeedback.builder()
                .description(FEEDBACK_DESCRIPTION)
                .memberId(memberId)
                .teamId(teamId)
                .submissionItem(submissionItem)
                .build();
    }

    public static ContestSubmissionFeedback createReadFeedback(final ContestSubmissionItem submissionItem,
                                                               final Long teamId, final Long memberId) {
        final ContestSubmissionFeedback feedback = ContestSubmissionFeedback.builder()
                .description(FEEDBACK_DESCRIPTION)
                .memberId(memberId)
                .teamId(teamId)
                .submissionItem(submissionItem)
                .build();
        feedback.markAsRead();
        return feedback;
    }
}
