package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record TeamMemberJoinNotificationEvent(
        List<Long> memberIds,
        Long teamId,
        String teamDisplayName
) {
}
