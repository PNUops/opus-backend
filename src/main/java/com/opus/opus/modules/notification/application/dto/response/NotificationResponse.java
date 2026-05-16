package com.opus.opus.modules.notification.application.dto.response;

import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(

        Long id,

        String title,

        String content,

        NotificationType targetType,

        Long targetId,

        String redirectUrl,

        Boolean isRead,

        LocalDateTime createdAt
) {

    public static NotificationResponse from(final Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getTargetId(),
                notification.getRedirectUrl(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
