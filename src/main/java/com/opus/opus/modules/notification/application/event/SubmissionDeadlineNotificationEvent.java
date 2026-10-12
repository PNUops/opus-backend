package com.opus.opus.modules.notification.application.event;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionDeadlineNotificationEvent(
        Long contestId,
        Long submissionItemId,
        String submissionItemName,
        int daysLeft,
        LocalDateTime endAt,
        List<SubmissionDeadlineTeam> teams
) {
}
