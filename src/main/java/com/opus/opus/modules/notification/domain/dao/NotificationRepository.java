package com.opus.opus.modules.notification.domain.dao;

import com.opus.opus.modules.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdAndMemberId(final Long id, final Long memberId);

    List<Notification> findAllByMemberIdAndIsReadFalse(final Long memberId);

    List<Notification> findTop20ByMemberIdOrderByCreatedAtDesc(final Long memberId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP WHERE n.memberId = :memberId AND n.isRead = false AND n.isDeleted = false")
    void updateAllUnreadByMemberId(@Param("memberId") Long memberId);
}
