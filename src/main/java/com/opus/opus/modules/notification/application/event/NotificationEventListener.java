package com.opus.opus.modules.notification.application.event;

import com.opus.opus.modules.notification.application.convenience.NotificationConvenience;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationConvenience notificationConvenience;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamCommentNotification(final TeamCommentNotificationEvent event) {
        try {
            notificationConvenience.sendTeamCommentNotifications(
                    event.memberIds(), event.teamId(), event.teamDisplayName());
        } catch (Exception e) {
            log.error("팀 댓글 알림 전송 실패 - teamId: {}", event.teamId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamMemberJoinNotification(final TeamMemberJoinNotificationEvent event) {
        try {
            notificationConvenience.sendTeamMemberJoinNotifications(
                    event.memberIds(), event.teamId(), event.teamDisplayName());
        } catch (Exception e) {
            log.error("팀 합류 알림 전송 실패 - teamId: {}", event.teamId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamAwardNotification(final TeamAwardNotificationEvent event) {
        try {
            notificationConvenience.sendTeamAwardNotifications(
                    event.memberIds(), event.teamId(), event.teamDisplayName());
        } catch (Exception e) {
            log.error("팀 수상 알림 전송 실패 - teamId: {}", event.teamId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionCompletedNotification(final SubmissionCompletedNotificationEvent event) {
        try {
            notificationConvenience.sendSubmissionCompletedNotifications(
                    event.memberIds(), event.contestId(), event.teamId(), event.submissionId(),
                    event.submissionItemId(), event.submissionItemName());
        } catch (Exception e) {
            log.error("제출 완료 알림 전송 실패 - submissionId: {}", event.submissionId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubmissionFeedbackNotification(final SubmissionFeedbackNotificationEvent event) {
        try {
            notificationConvenience.sendSubmissionFeedbackNotifications(
                    event.memberIds(), event.contestId(), event.teamId(), event.submissionId(),
                    event.submissionItemId(), event.submissionItemName());
        } catch (Exception e) {
            log.error("새 피드백 알림 전송 실패 - submissionId: {}", event.submissionId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStaffAssignedNotification(final StaffAssignedNotificationEvent event) {
        try {
            notificationConvenience.sendStaffAssignedNotifications(
                    event.contestId(), event.staffId(), event.staffName(), event.position(), event.teams());
        } catch (Exception e) {
            log.error("{} 지정 알림 전송 실패 - staffId: {}", event.position().getPositionName(), event.staffId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStaffUnassignedNotification(final StaffUnassignedNotificationEvent event) {
        try {
            notificationConvenience.sendStaffUnassignedNotifications(
                    event.contestId(), event.staffId(), event.staffName(), event.position(), event.teams());
        } catch (Exception e) {
            log.error("{} 해제 알림 전송 실패 - staffId: {}", event.position().getPositionName(), event.staffId(), e);
        }
    }
}
