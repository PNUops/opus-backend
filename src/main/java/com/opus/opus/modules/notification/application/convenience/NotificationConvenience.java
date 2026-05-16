package com.opus.opus.modules.notification.application.convenience;

import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationConvenience {

    private final NotificationRepository notificationRepository;

    public void sendNotificationsToMembers(final List<Long> memberIds, final NotificationType type,
                                           final String title, final String content,
                                           final Long targetId, final String redirectUrl) {
        if (memberIds.isEmpty()) {
            return;
        }
        final List<Notification> notifications = memberIds.stream()
                .map(memberId -> Notification.builder()
                        .memberId(memberId)
                        .type(type)
                        .title(title)
                        .content(content)
                        .targetId(targetId)
                        .redirectUrl(redirectUrl)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }
}
