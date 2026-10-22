package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record SubmissionFeedbackNotificationEvent(
        List<Long> memberIds,
        Long contestId,
        Long teamId,
        Long submissionId,
        Long submissionItemId,
        String submissionItemName
) {
}
