package com.opus.opus.modules.notification.application.convenience;

import static com.opus.opus.modules.notification.domain.NotificationType.SUBMISSION_COMPLETED;
import static com.opus.opus.modules.notification.domain.NotificationType.SUBMISSION_FEEDBACK;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_AWARDS;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_COMMENT;

import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class NotificationConvenience {

    private final NotificationRepository notificationRepository;

    public void sendTeamMemberJoinNotifications(final List<Long> memberIds, final Long teamId,
                                                final String teamDisplayName) {
        save(memberIds, TEAM, "팀 합류 알림",
                teamDisplayName + " 팀의 팀원이 되었습니다.", teamId, "/teams/" + teamId);
    }

    public void sendTeamAwardNotifications(final List<Long> memberIds, final Long teamId,
                                           final String teamDisplayName) {
        save(memberIds, TEAM_AWARDS, "수상 알림",
                teamDisplayName + " 팀이 수상했습니다.", teamId, "/teams/" + teamId);
    }

    public void sendTeamCommentNotifications(final List<Long> memberIds, final Long teamId,
                                             final String teamDisplayName) {
        save(memberIds, TEAM_COMMENT, "새 댓글 알림",
                teamDisplayName + " 팀에 새 댓글이 달렸습니다.", teamId, "/teams/" + teamId);
    }

    public void sendSubmissionCompletedNotifications(final List<Long> memberIds, final Long contestId,
                                                     final Long teamId, final Long submissionId,
                                                     final Long submissionItemId, final String submissionItemName) {
        save(memberIds, SUBMISSION_COMPLETED, "제출 완료 알림",
                submissionItemName + " 제출이 완료되었습니다.", submissionId,
                toSubmissionRedirectUrl(contestId, teamId, submissionItemId));
    }

    public void sendSubmissionFeedbackNotifications(final List<Long> memberIds, final Long contestId,
                                                    final Long teamId, final Long submissionId,
                                                    final Long submissionItemId, final String submissionItemName) {
        save(memberIds, SUBMISSION_FEEDBACK, "새 피드백 알림",
                submissionItemName + "에 새로운 피드백이 등록되었습니다.", submissionId,
                toSubmissionRedirectUrl(contestId, teamId, submissionItemId));
    }

    private String toSubmissionRedirectUrl(final Long contestId, final Long teamId, final Long submissionItemId) {
        return "/me/contests/" + contestId + "/teams/" + teamId + "/submissions?submissionItemId=" + submissionItemId;
    }

    private void save(final List<Long> memberIds, final NotificationType type,
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
