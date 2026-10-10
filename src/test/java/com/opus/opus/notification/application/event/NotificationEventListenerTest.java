package com.opus.opus.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.notification.application.event.TeamCommentNotificationEvent;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class NotificationEventListenerTest extends IntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Long MEMBER_ID_1 = 1L;
    private static final Long MEMBER_ID_2 = 2L;
    private static final Long TEAM_ID = 1L;
    private static final String TEAM_DISPLAY_NAME = "테스트팀";

    @BeforeEach
    void 커밋된_알림을_정리한다() {
        notificationRepository.deleteAllInBatch();
    }

    @AfterEach
    void 테스트에서_커밋된_알림을_정리한다() {
        notificationRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] 이벤트를 발행한 트랜잭션이 커밋되면 알림이 저장된다.")
    void 이벤트를_발행한_트랜잭션이_커밋되면_알림이_저장된다() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new TeamCommentNotificationEvent(List.of(MEMBER_ID_1, MEMBER_ID_2), TEAM_ID, TEAM_DISPLAY_NAME)));

        final List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications).extracting(Notification::getMemberId)
                .containsExactlyInAnyOrder(MEMBER_ID_1, MEMBER_ID_2);
        assertThat(notifications).extracting(Notification::getType)
                .containsOnly(NotificationType.TEAM_COMMENT);
    }

    @Test
    @DisplayName("[성공] 이벤트를 발행한 트랜잭션이 롤백되면 알림이 저장되지 않는다.")
    void 이벤트를_발행한_트랜잭션이_롤백되면_알림이_저장되지_않는다() {
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(
                    new TeamCommentNotificationEvent(List.of(MEMBER_ID_1, MEMBER_ID_2), TEAM_ID, TEAM_DISPLAY_NAME));
            status.setRollbackOnly();
        });

        assertThat(notificationRepository.findAll()).isEmpty();
    }
}
