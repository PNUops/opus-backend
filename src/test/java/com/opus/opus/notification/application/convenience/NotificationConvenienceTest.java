package com.opus.opus.notification.application.convenience;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.convenience.NotificationConvenience;
import com.opus.opus.modules.notification.application.event.StaffAssignmentTeam;
import com.opus.opus.modules.notification.application.event.StaffPosition;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineTeam;
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
    private static final Long STAFF_ID = 100L;
    private static final String STAFF_NAME = "김교수";
    private static final String TEAM_DASHBOARD_URL = "/me/contests/1/teams/1/dashboard";

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
    @DisplayName("[성공] 제출 마감 알림이 미제출 팀의 팀원에게 생성된다.")
    void 제출_마감_알림이_미제출_팀의_팀원에게_생성된다() {
        final List<SubmissionDeadlineTeam> teams = List.of(
                new SubmissionDeadlineTeam(TEAM_ID, List.of(member1.getId())),
                new SubmissionDeadlineTeam(2L, List.of(member2.getId())));

        notificationConvenience.sendSubmissionDeadlineNotifications(
                CONTEST_ID, SUBMISSION_ITEM_ID, SUBMISSION_ITEM_NAME, 3, teams);

        final Notification member1Notification = getOnlyNotification(member1.getId());
        assertThat(member1Notification.getType()).isEqualTo(NotificationType.SUBMISSION_DEADLINE);
        assertThat(member1Notification.getTitle()).isEqualTo("제출 마감 알림");
        assertThat(member1Notification.getContent()).isEqualTo("중간보고서 제출 마감이 3일 남았습니다.");
        assertThat(member1Notification.getTargetId()).isEqualTo(SUBMISSION_ITEM_ID);
        assertThat(member1Notification.getRedirectUrl()).isEqualTo(SUBMISSION_REDIRECT_URL);
        assertThat(getOnlyNotification(member2.getId()).getRedirectUrl())
                .isEqualTo("/me/contests/1/teams/2/submissions?submissionItemId=20");
    }

    @Test
    @DisplayName("[성공] 제출 마감 하루 전 알림은 남은 일수가 1일로 생성된다.")
    void 제출_마감_하루_전_알림은_남은_일수가_1일로_생성된다() {
        notificationConvenience.sendSubmissionDeadlineNotifications(CONTEST_ID, SUBMISSION_ITEM_ID,
                SUBMISSION_ITEM_NAME, 1, List.of(new SubmissionDeadlineTeam(TEAM_ID, List.of(member1.getId()))));

        assertThat(getOnlyNotification(member1.getId()).getContent()).isEqualTo("중간보고서 제출 마감이 1일 남았습니다.");
    }

    @Test
    @DisplayName("[성공] 지도교수 지정 알림이 팀원과 지정된 교수에게 생성된다.")
    void 지도교수_지정_알림이_팀원과_지정된_교수에게_생성된다() {
        notificationConvenience.sendStaffAssignedNotifications(
                CONTEST_ID, STAFF_ID, STAFF_NAME, StaffPosition.ADVISOR, List.of(createStaffAssignmentTeam()));

        final Notification memberNotification = getOnlyNotification(member1.getId());
        assertThat(memberNotification.getType()).isEqualTo(NotificationType.ADVISOR_ASSIGNED);
        assertThat(memberNotification.getTitle()).isEqualTo("지도교수 지정 알림");
        assertThat(memberNotification.getContent()).isEqualTo("김교수 교수님이 지도교수로 지정되었습니다.");
        assertThat(memberNotification.getTargetId()).isEqualTo(TEAM_ID);
        assertThat(memberNotification.getRedirectUrl()).isEqualTo(TEAM_DASHBOARD_URL);

        final Notification staffNotification = getOnlyNotification(STAFF_ID);
        assertThat(staffNotification.getType()).isEqualTo(NotificationType.ADVISOR_ASSIGNED);
        assertThat(staffNotification.getContent()).isEqualTo("테스트팀 팀의 지도교수로 지정되었습니다.");
        assertThat(staffNotification.getTargetId()).isEqualTo(TEAM_ID);
        assertThat(staffNotification.getRedirectUrl()).isEqualTo("/me/advisor-activity/contests/1");
    }

    @Test
    @DisplayName("[성공] 지도교수 해제 알림이 팀원과 해제된 교수에게 생성된다.")
    void 지도교수_해제_알림이_팀원과_해제된_교수에게_생성된다() {
        notificationConvenience.sendStaffUnassignedNotifications(
                CONTEST_ID, STAFF_ID, STAFF_NAME, StaffPosition.ADVISOR, List.of(createStaffAssignmentTeam()));

        final Notification memberNotification = getOnlyNotification(member1.getId());
        assertThat(memberNotification.getType()).isEqualTo(NotificationType.ADVISOR_UNASSIGNED);
        assertThat(memberNotification.getTitle()).isEqualTo("지도교수 해제 알림");
        assertThat(memberNotification.getContent()).isEqualTo("김교수 교수님이 지도교수에서 해제되었습니다.");
        assertThat(memberNotification.getRedirectUrl()).isEqualTo(TEAM_DASHBOARD_URL);

        final Notification staffNotification = getOnlyNotification(STAFF_ID);
        assertThat(staffNotification.getContent()).isEqualTo("테스트팀 팀의 지도교수에서 해제되었습니다.");
        assertThat(staffNotification.getRedirectUrl()).isEqualTo("/contest/1/teams/view/1");
    }

    @Test
    @DisplayName("[성공] 멘토 지정 알림이 멘토 문구로 생성된다.")
    void 멘토_지정_알림이_멘토_문구로_생성된다() {
        notificationConvenience.sendStaffAssignedNotifications(
                CONTEST_ID, STAFF_ID, "박멘토", StaffPosition.MENTOR, List.of(createStaffAssignmentTeam()));

        final Notification memberNotification = getOnlyNotification(member1.getId());
        assertThat(memberNotification.getType()).isEqualTo(NotificationType.MENTOR_ASSIGNED);
        assertThat(memberNotification.getTitle()).isEqualTo("멘토 지정 알림");
        assertThat(memberNotification.getContent()).isEqualTo("박멘토 멘토님이 멘토로 지정되었습니다.");
        assertThat(getOnlyNotification(STAFF_ID).getContent()).isEqualTo("테스트팀 팀의 멘토로 지정되었습니다.");
    }

    @Test
    @DisplayName("[성공] 멘토 해제 알림이 멘토 문구로 생성된다.")
    void 멘토_해제_알림이_멘토_문구로_생성된다() {
        notificationConvenience.sendStaffUnassignedNotifications(
                CONTEST_ID, STAFF_ID, "박멘토", StaffPosition.MENTOR, List.of(createStaffAssignmentTeam()));

        final Notification memberNotification = getOnlyNotification(member1.getId());
        assertThat(memberNotification.getType()).isEqualTo(NotificationType.MENTOR_UNASSIGNED);
        assertThat(memberNotification.getTitle()).isEqualTo("멘토 해제 알림");
        assertThat(memberNotification.getContent()).isEqualTo("박멘토 멘토님이 멘토에서 해제되었습니다.");
        assertThat(getOnlyNotification(STAFF_ID).getContent()).isEqualTo("테스트팀 팀의 멘토에서 해제되었습니다.");
    }

    @Test
    @DisplayName("[성공] 여러 팀에 지정되면 교수는 팀마다 알림을 받는다.")
    void 여러_팀에_지정되면_교수는_팀마다_알림을_받는다() {
        final StaffAssignmentTeam otherTeam = new StaffAssignmentTeam(2L, "다른팀", List.of(member2.getId()));

        notificationConvenience.sendStaffAssignedNotifications(CONTEST_ID, STAFF_ID, STAFF_NAME,
                StaffPosition.ADVISOR, List.of(createStaffAssignmentTeam(), otherTeam));

        assertThat(notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(STAFF_ID))
                .extracting(Notification::getTargetId)
                .containsExactlyInAnyOrder(TEAM_ID, 2L);
        assertThat(notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(member2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("[성공] 알림 대상 회원이 없으면 알림이 생성되지 않는다.")
    void 알림_대상_회원이_없으면_알림이_생성되지_않는다() {
        notificationConvenience.sendTeamMemberJoinNotifications(List.of(), TEAM_ID, TEAM_DISPLAY_NAME);

        assertThat(notificationRepository.findAll()).isEmpty();
    }

    private StaffAssignmentTeam createStaffAssignmentTeam() {
        return new StaffAssignmentTeam(TEAM_ID, TEAM_DISPLAY_NAME, List.of(member1.getId()));
    }

    private Notification getOnlyNotification(final Long memberId) {
        final List<Notification> notifications = notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(
                memberId);
        assertThat(notifications).hasSize(1);
        return notifications.get(0);
    }
}
