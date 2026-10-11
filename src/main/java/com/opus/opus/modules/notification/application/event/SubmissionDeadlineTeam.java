package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record SubmissionDeadlineTeam(
        Long teamId,
        List<Long> memberIds
) {
}
