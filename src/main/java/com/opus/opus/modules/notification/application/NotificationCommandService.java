package com.opus.opus.modules.notification.application;

import static com.opus.opus.modules.notification.exception.NotificationExceptionType.NOT_FOUND_NOTIFICATION;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import com.opus.opus.modules.notification.exception.NotificationException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    private final MemberConvenience memberConvenience;

    public void updateSingleNotification(final Long notificationId, final Member member) {
        memberConvenience.validateExistMember(member.getId());

        final Notification notification = notificationRepository.findByIdAndMemberId(notificationId, member.getId())
                .orElseThrow(() -> new NotificationException(NOT_FOUND_NOTIFICATION));

        notification.updateNotification();
    }

    public void updateNotificationAll(final Member member) {
        memberConvenience.validateExistMember(member.getId());

        final List<Notification> unreadNotifications = notificationRepository.findAllByMemberIdAndIsReadFalse(
                member.getId());

        unreadNotifications.forEach(Notification::updateNotification);
    }
}
