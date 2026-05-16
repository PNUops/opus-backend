package com.opus.opus.notification;

import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;

public class NotificationFixture {

    public static Notification createTeamNotification(final Long memberId, final Long targetId) {
        return Notification.builder()
                .memberId(memberId)
                .title("팀 알림 제목입니다.")
                .content("팀 알림 내용입니다.")
                .type(NotificationType.TEAM)
                .targetId(targetId)
                .redirectUrl("/teams/" + targetId)
                .build();
    }

    public static Notification createTeamCommentNotification(final Long memberId, final Long targetId) {
        return Notification.builder()
                .memberId(memberId)
                .title("팀 댓글 알림 제목입니다.")
                .content("팀 댓글 알림 내용입니다.")
                .type(NotificationType.TEAM_COMMENT)
                .targetId(targetId)
                .redirectUrl("/teams/" + targetId)
                .build();
    }

    public static Notification createTeamAwardsNotification(final Long memberId, final Long targetId) {
        return Notification.builder()
                .memberId(memberId)
                .title("팀 수상 알림 제목입니다.")
                .content("팀 수상 알림 내용입니다.")
                .type(NotificationType.TEAM_AWARDS)
                .targetId(targetId)
                .redirectUrl("/teams/" + targetId)
                .build();
    }
}
