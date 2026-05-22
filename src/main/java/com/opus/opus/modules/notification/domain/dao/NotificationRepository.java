package com.opus.opus.modules.notification.domain.dao;

import com.opus.opus.modules.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndMemberId(final Long id, final Long memberId);

    List<Notification> findAllByMemberIdAndIsReadFalse(final Long memberId);

    List<Notification> findAllByMemberIdOrderByCreatedAtDesc(final Long memberId);
}
