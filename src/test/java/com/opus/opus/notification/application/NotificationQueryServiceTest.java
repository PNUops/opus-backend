package com.opus.opus.notification.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.NotificationQueryService;
import com.opus.opus.modules.notification.application.dto.response.NotificationResponse;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import com.opus.opus.notification.NotificationFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificationQueryServiceTest extends IntegrationTest {

    @Autowired
    private NotificationQueryService notificationQueryService;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Member member;
    private Notification notification;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createMember());
        notification = notificationRepository.save(NotificationFixture.createTeamNotification(member.getId(), 1L));
    }

    @Test
    @DisplayName("[성공] 회원의 알림 목록을 조회할 수 있다.")
    void 회원의_알림_목록을_조회할_수_있다() {
        final Notification anotherNotification = notificationRepository.save(
                NotificationFixture.createTeamCommentNotification(member.getId(), 1L));

        final List<NotificationResponse> responses = notificationQueryService.getNotifications(member);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(NotificationResponse::id)
                .containsExactlyInAnyOrder(notification.getId(), anotherNotification.getId());
    }

    @Test
    @DisplayName("[성공] 알림의 내용이 올바르게 반환된다.")
    void 알림의_내용이_올바르게_반환된다() {
        final List<NotificationResponse> responses = notificationQueryService.getNotifications(member);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(notification.getId());
        assertThat(responses.get(0).title()).isEqualTo(notification.getTitle());
        assertThat(responses.get(0).content()).isEqualTo(notification.getContent());
        assertThat(responses.get(0).targetType()).isEqualTo(NotificationType.TEAM);
        assertThat(responses.get(0).isRead()).isFalse();
    }
}
