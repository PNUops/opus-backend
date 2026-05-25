package com.opus.opus.modules.notification.application.event;

import java.util.List;

public record TeamCommentNotificationEvent(
        List<Long> memberIds,
        Long teamId,
        String teamDisplayName
) {
}
