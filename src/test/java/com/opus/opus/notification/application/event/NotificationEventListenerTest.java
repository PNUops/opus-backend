package com.opus.opus.notification.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.notification.application.event.StaffAssignedNotificationEvent;
import com.opus.opus.modules.notification.application.event.StaffAssignmentTeam;
import com.opus.opus.modules.notification.application.event.StaffPosition;
import com.opus.opus.modules.notification.application.event.StaffUnassignedNotificationEvent;
import com.opus.opus.modules.notification.application.event.SubmissionCompletedNotificationEvent;
import com.opus.opus.modules.notification.application.event.SubmissionFeedbackNotificationEvent;
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
    private static final Long CONTEST_ID = 1L;
    private static final Long SUBMISSION_ID = 10L;
    private static final Long SUBMISSION_ITEM_ID = 20L;
    private static final String SUBMISSION_ITEM_NAME = "중간보고서";
    private static final Long STAFF_ID = 100L;
    private static final String STAFF_NAME = "김교수";

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

    @Test
    @DisplayName("[성공] 제출 완료 이벤트를 발행한 트랜잭션이 커밋되면 제출 완료 알림이 저장된다.")
    void 제출_완료_이벤트를_발행한_트랜잭션이_커밋되면_제출_완료_알림이_저장된다() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new SubmissionCompletedNotificationEvent(List.of(MEMBER_ID_1, MEMBER_ID_2), CONTEST_ID, TEAM_ID,
                        SUBMISSION_ID, SUBMISSION_ITEM_ID, SUBMISSION_ITEM_NAME)));

        final List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications).extracting(Notification::getType)
                .containsOnly(NotificationType.SUBMISSION_COMPLETED);
    }

    @Test
    @DisplayName("[성공] 새 피드백 이벤트를 발행한 트랜잭션이 커밋되면 새 피드백 알림이 저장된다.")
    void 새_피드백_이벤트를_발행한_트랜잭션이_커밋되면_새_피드백_알림이_저장된다() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new SubmissionFeedbackNotificationEvent(List.of(MEMBER_ID_1, MEMBER_ID_2), CONTEST_ID, TEAM_ID,
                        SUBMISSION_ID, SUBMISSION_ITEM_ID, SUBMISSION_ITEM_NAME)));

        final List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(2);
        assertThat(notifications).extracting(Notification::getType)
                .containsOnly(NotificationType.SUBMISSION_FEEDBACK);
    }

    @Test
    @DisplayName("[성공] 지정 이벤트를 발행한 트랜잭션이 커밋되면 팀원과 교수에게 지정 알림이 저장된다.")
    void 지정_이벤트를_발행한_트랜잭션이_커밋되면_팀원과_교수에게_지정_알림이_저장된다() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new StaffAssignedNotificationEvent(CONTEST_ID, STAFF_ID, STAFF_NAME, StaffPosition.ADVISOR,
                        List.of(new StaffAssignmentTeam(TEAM_ID, TEAM_DISPLAY_NAME,
                                List.of(MEMBER_ID_1, MEMBER_ID_2))))));

        final List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::getMemberId)
                .containsExactlyInAnyOrder(MEMBER_ID_1, MEMBER_ID_2, STAFF_ID);
        assertThat(notifications).extracting(Notification::getType)
                .containsOnly(NotificationType.ADVISOR_ASSIGNED);
    }

    @Test
    @DisplayName("[성공] 해제 이벤트를 발행한 트랜잭션이 커밋되면 팀원과 멘토에게 해제 알림이 저장된다.")
    void 해제_이벤트를_발행한_트랜잭션이_커밋되면_팀원과_멘토에게_해제_알림이_저장된다() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new StaffUnassignedNotificationEvent(CONTEST_ID, STAFF_ID, STAFF_NAME, StaffPosition.MENTOR,
                        List.of(new StaffAssignmentTeam(TEAM_ID, TEAM_DISPLAY_NAME,
                                List.of(MEMBER_ID_1, MEMBER_ID_2))))));

        final List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(Notification::getMemberId)
                .containsExactlyInAnyOrder(MEMBER_ID_1, MEMBER_ID_2, STAFF_ID);
        assertThat(notifications).extracting(Notification::getType)
                .containsOnly(NotificationType.MENTOR_UNASSIGNED);
    }
}
