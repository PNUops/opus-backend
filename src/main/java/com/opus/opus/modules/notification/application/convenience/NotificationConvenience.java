package com.opus.opus.modules.notification.application.convenience;

import static com.opus.opus.modules.notification.domain.NotificationType.TEAM;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_AWARDS;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_COMMENT;

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
