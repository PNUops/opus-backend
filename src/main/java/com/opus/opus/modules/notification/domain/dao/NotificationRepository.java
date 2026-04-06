package com.opus.opus.modules.notification.domain.dao;

import com.opus.opus.modules.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
