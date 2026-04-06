package com.opus.opus.modules.notification.domain.dao;

import com.opus.opus.modules.notification.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByMemberIdAndIsReadFalse(final Long memberId);
}
