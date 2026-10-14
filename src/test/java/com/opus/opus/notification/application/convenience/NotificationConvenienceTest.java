package com.opus.opus.notification.application.convenience;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.convenience.NotificationConvenience;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

public class NotificationConvenienceTest extends IntegrationTest {

    @Autowired
    private NotificationConvenience notificationConvenience;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private static final Long TEAM_ID = 1L;
    private static final String TEAM_DISPLAY_NAME = "테스트팀";
    private static final Long CONTEST_ID = 1L;
    private static final Long SUBMISSION_ID = 10L;
    private static final Long SUBMISSION_ITEM_ID = 20L;
    private static final String SUBMISSION_ITEM_NAME = "중간보고서";
    private static final String SUBMISSION_REDIRECT_URL = "/me/contests/1/teams/1/submissions?submissionItemId=20";

    @BeforeTransaction
    void 커밋된_알림을_정리한다() {
        notificationRepository.deleteAllInBatch();
    }

    @AfterTransaction
    void 테스트에서_커밋된_알림을_정리한다() {
        notificationRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        member2 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));
    }

    @Test
    @DisplayName("[성공] 팀 합류 알림이 회원 수만큼 생성된다.")
    void 팀_합류_알림이_회원_수만큼_생성된다() {
        final List<Long> memberIds = List.of(member1.getId(), member2.getId());

        notificationConvenience.sendTeamMemberJoinNotifications(memberIds, TEAM_ID, TEAM_DISPLAY_NAME);

        final List<Notification> member1Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member1.getId());
        final List<Notification> member2Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member2.getId());
        assertThat(member1Notifications).hasSize(1);
        assertThat(member2Notifications).hasSize(1);
        assertThat(member1Notifications.get(0).getType()).isEqualTo(NotificationType.TEAM);
        assertThat(member1Notifications.get(0).getTitle()).isEqualTo("팀 합류 알림");
        assertThat(member1Notifications.get(0).getContent()).contains(TEAM_DISPLAY_NAME);
        assertThat(member1Notifications.get(0).getTargetId()).isEqualTo(TEAM_ID);
        assertThat(member1Notifications.get(0).getRedirectUrl()).isEqualTo("/teams/" + TEAM_ID);
    }

    @Test
    @DisplayName("[성공] 팀 수상 알림이 회원 수만큼 생성된다.")
    void 팀_수상_알림이_회원_수만큼_생성된다() {
        final List<Long> memberIds = List.of(member1.getId(), member2.getId());

        notificationConvenience.sendTeamAwardNotifications(memberIds, TEAM_ID, TEAM_DISPLAY_NAME);

        final List<Notification> member1Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member1.getId());
        final List<Notification> member2Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member2.getId());
        assertThat(member1Notifications).hasSize(1);
        assertThat(member2Notifications).hasSize(1);
        assertThat(member1Notifications.get(0).getType()).isEqualTo(NotificationType.TEAM_AWARDS);
        assertThat(member1Notifications.get(0).getTitle()).isEqualTo("수상 알림");
        assertThat(member1Notifications.get(0).getContent()).contains(TEAM_DISPLAY_NAME);
        assertThat(member1Notifications.get(0).getTargetId()).isEqualTo(TEAM_ID);
        assertThat(member1Notifications.get(0).getRedirectUrl()).isEqualTo("/teams/" + TEAM_ID);
    }

    @Test
    @DisplayName("[성공] 팀 댓글 알림이 회원 수만큼 생성된다.")
    void 팀_댓글_알림이_회원_수만큼_생성된다() {
        final List<Long> memberIds = List.of(member1.getId(), member2.getId());

        notificationConvenience.sendTeamCommentNotifications(memberIds, TEAM_ID, TEAM_DISPLAY_NAME);

        final List<Notification> member1Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member1.getId());
        final List<Notification> member2Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member2.getId());
        assertThat(member1Notifications).hasSize(1);
        assertThat(member2Notifications).hasSize(1);
        assertThat(member1Notifications.get(0).getType()).isEqualTo(NotificationType.TEAM_COMMENT);
        assertThat(member1Notifications.get(0).getTitle()).isEqualTo("새 댓글 알림");
        assertThat(member1Notifications.get(0).getContent()).contains(TEAM_DISPLAY_NAME);
        assertThat(member1Notifications.get(0).getTargetId()).isEqualTo(TEAM_ID);
        assertThat(member1Notifications.get(0).getRedirectUrl()).isEqualTo("/teams/" + TEAM_ID);
    }

    @Test
    @DisplayName("[성공] 제출 완료 알림이 회원 수만큼 생성된다.")
    void 제출_완료_알림이_회원_수만큼_생성된다() {
        final List<Long> memberIds = List.of(member1.getId(), member2.getId());

        notificationConvenience.sendSubmissionCompletedNotifications(
                memberIds, CONTEST_ID, TEAM_ID, SUBMISSION_ID, SUBMISSION_ITEM_ID, SUBMISSION_ITEM_NAME);

        final List<Notification> member1Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member1.getId());
        final List<Notification> member2Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member2.getId());
        assertThat(member1Notifications).hasSize(1);
        assertThat(member2Notifications).hasSize(1);
        assertThat(member1Notifications.get(0).getType()).isEqualTo(NotificationType.SUBMISSION_COMPLETED);
        assertThat(member1Notifications.get(0).getTitle()).isEqualTo("제출 완료 알림");
        assertThat(member1Notifications.get(0).getContent()).isEqualTo("중간보고서 제출이 완료되었습니다.");
        assertThat(member1Notifications.get(0).getTargetId()).isEqualTo(SUBMISSION_ID);
        assertThat(member1Notifications.get(0).getRedirectUrl()).isEqualTo(SUBMISSION_REDIRECT_URL);
    }

    @Test
    @DisplayName("[성공] 새 피드백 알림이 회원 수만큼 생성된다.")
    void 새_피드백_알림이_회원_수만큼_생성된다() {
        final List<Long> memberIds = List.of(member1.getId(), member2.getId());

        notificationConvenience.sendSubmissionFeedbackNotifications(
                memberIds, CONTEST_ID, TEAM_ID, SUBMISSION_ID, SUBMISSION_ITEM_ID, SUBMISSION_ITEM_NAME);

        final List<Notification> member1Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member1.getId());
        final List<Notification> member2Notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                member2.getId());
        assertThat(member1Notifications).hasSize(1);
        assertThat(member2Notifications).hasSize(1);
        assertThat(member1Notifications.get(0).getType()).isEqualTo(NotificationType.SUBMISSION_FEEDBACK);
        assertThat(member1Notifications.get(0).getTitle()).isEqualTo("새 피드백 알림");
        assertThat(member1Notifications.get(0).getContent()).isEqualTo("중간보고서에 새로운 피드백이 등록되었습니다.");
        assertThat(member1Notifications.get(0).getTargetId()).isEqualTo(SUBMISSION_ID);
        assertThat(member1Notifications.get(0).getRedirectUrl()).isEqualTo(SUBMISSION_REDIRECT_URL);
    }

    @Test
    @DisplayName("[성공] 알림 대상 회원이 없으면 알림이 생성되지 않는다.")
    void 알림_대상_회원이_없으면_알림이_생성되지_않는다() {
        notificationConvenience.sendTeamMemberJoinNotifications(List.of(), TEAM_ID, TEAM_DISPLAY_NAME);

        assertThat(notificationRepository.findAll()).isEmpty();
    }
}
