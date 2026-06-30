package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record TeamDashboardSummaryResponse(
        long pendingSubmissionCount,
        LocalDateTime nearestDeadline,
        long unreadFeedbackCount,
        String latestFeedbackPreview
) {
}
