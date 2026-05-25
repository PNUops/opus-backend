package com.opus.opus.notification.application;

import static com.opus.opus.modules.notification.exception.NotificationExceptionType.NOT_FOUND_NOTIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.NotificationCommandService;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import com.opus.opus.modules.notification.exception.NotificationException;
import com.opus.opus.notification.NotificationFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificationCommandServiceTest extends IntegrationTest {

    @Autowired
    private NotificationCommandService notificationCommandService;

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
    @DisplayName("[성공] 단일 알림이 읽음으로 처리된다.")
    void 단일_알림이_읽음으로_처리된다() {
        notificationCommandService.updateSingleNotification(notification.getId(), member);

        final Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("[실패] 타인의 알림을 읽음 처리하면 예외가 발생한다")
    void 타인의_알림을_읽음_처리하면_예외가_발생한다() {
        final Member anotherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));

        assertThatThrownBy(() ->
                notificationCommandService.updateSingleNotification(notification.getId(), anotherMember))
                .isInstanceOf(NotificationException.class)
                .satisfies(ex -> assertThat(((NotificationException) ex).exceptionType())
                        .isEqualTo(NOT_FOUND_NOTIFICATION));
    }

    @Test
    @DisplayName("[성공] 회원의 모든 알림이 읽음으로 처리된다.")
    void 회원의_모든_알림이_읽음으로_처리된다() {
        notificationRepository.save(NotificationFixture.createTeamCommentNotification(member.getId(), 1L));
        notificationRepository.save(NotificationFixture.createTeamAwardsNotification(member.getId(), 1L));

        notificationCommandService.updateNotificationAll(member);

        final List<Notification> unreadNotifications = notificationRepository.findAllByMemberIdAndIsReadFalse(
                member.getId());
        assertThat(unreadNotifications).isEmpty();
    }
}
