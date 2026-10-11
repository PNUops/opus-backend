package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record SubmissionDeadlineNotificationEvent(
        Long contestId,
        Long submissionItemId,
        String submissionItemName,
        int daysLeft,
        List<SubmissionDeadlineTeam> teams
) {
}
