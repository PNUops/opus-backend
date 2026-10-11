package com.opus.opus.modules.notification.application.convenience;

import static com.opus.opus.modules.notification.domain.NotificationType.SUBMISSION_COMPLETED;
import static com.opus.opus.modules.notification.domain.NotificationType.SUBMISSION_DEADLINE;
import static com.opus.opus.modules.notification.domain.NotificationType.SUBMISSION_FEEDBACK;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_AWARDS;
import static com.opus.opus.modules.notification.domain.NotificationType.TEAM_COMMENT;

import com.opus.opus.modules.notification.application.event.StaffAssignmentTeam;
import com.opus.opus.modules.notification.application.event.StaffPosition;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineTeam;
import com.opus.opus.modules.notification.domain.Notification;
import com.opus.opus.modules.notification.domain.NotificationType;
import com.opus.opus.modules.notification.domain.dao.NotificationRepository;
import java.util.List;
import java.util.stream.Stream;
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

    public void sendSubmissionDeadlineNotifications(final Long contestId, final Long submissionItemId,
                                                    final String submissionItemName, final int daysLeft,
                                                    final List<SubmissionDeadlineTeam> teams) {
        final String content = submissionItemName + " 제출 마감이 " + daysLeft + "일 남았습니다.";
        final List<Notification> notifications = teams.stream()
                .flatMap(team -> toNotifications(team.memberIds(), SUBMISSION_DEADLINE, "제출 마감 알림", content,
                        submissionItemId, toSubmissionRedirectUrl(contestId, team.teamId(), submissionItemId)).stream())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    public void sendStaffAssignedNotifications(final Long contestId, final Long staffId, final String staffName,
                                               final StaffPosition position, final List<StaffAssignmentTeam> teams) {
        final List<Notification> notifications = teams.stream()
                .flatMap(team -> toStaffAssignedNotifications(contestId, staffId, staffName, position, team))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    public void sendStaffUnassignedNotifications(final Long contestId, final Long staffId, final String staffName,
                                                 final StaffPosition position, final List<StaffAssignmentTeam> teams) {
        final List<Notification> notifications = teams.stream()
                .flatMap(team -> toStaffUnassignedNotifications(contestId, staffId, staffName, position, team))
                .toList();
        notificationRepository.saveAll(notifications);
    }

    private Stream<Notification> toStaffAssignedNotifications(final Long contestId, final Long staffId,
                                                              final String staffName, final StaffPosition position,
                                                              final StaffAssignmentTeam team) {
        final NotificationType type = position.getAssignedType();
        final String title = position.getPositionName() + " 지정 알림";
        final String change = position.getPositionName() + "로 지정되었습니다.";
        return Stream.concat(
                toNotifications(team.memberIds(), type, title,
                        staffName + " " + position.getHonorific() + "이 " + change,
                        team.teamId(), toTeamDashboardUrl(contestId, team.teamId())).stream(),
                Stream.of(toNotification(staffId, type, title, team.teamDisplayName() + " 팀의 " + change,
                        team.teamId(), toAdvisorActivityUrl(contestId))));
    }

    private Stream<Notification> toStaffUnassignedNotifications(final Long contestId, final Long staffId,
                                                                final String staffName, final StaffPosition position,
                                                                final StaffAssignmentTeam team) {
        final NotificationType type = position.getUnassignedType();
        final String title = position.getPositionName() + " 해제 알림";
        final String change = position.getPositionName() + "에서 해제되었습니다.";
        return Stream.concat(
                toNotifications(team.memberIds(), type, title,
                        staffName + " " + position.getHonorific() + "이 " + change,
                        team.teamId(), toTeamDashboardUrl(contestId, team.teamId())).stream(),
                Stream.of(toNotification(staffId, type, title, team.teamDisplayName() + " 팀의 " + change,
                        team.teamId(), toProjectDetailUrl(contestId, team.teamId()))));
    }

    private String toSubmissionRedirectUrl(final Long contestId, final Long teamId, final Long submissionItemId) {
        return "/me/contests/" + contestId + "/teams/" + teamId + "/submissions?submissionItemId=" + submissionItemId;
    }

    private String toTeamDashboardUrl(final Long contestId, final Long teamId) {
        return "/me/contests/" + contestId + "/teams/" + teamId + "/dashboard";
    }

    private String toAdvisorActivityUrl(final Long contestId) {
        return "/me/advisor-activity/contests/" + contestId;
    }

    private String toProjectDetailUrl(final Long contestId, final Long teamId) {
        return "/contest/" + contestId + "/teams/view/" + teamId;
    }

    private void save(final List<Long> memberIds, final NotificationType type,
                      final String title, final String content,
                      final Long targetId, final String redirectUrl) {
        if (memberIds.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(toNotifications(memberIds, type, title, content, targetId, redirectUrl));
    }

    private List<Notification> toNotifications(final List<Long> memberIds, final NotificationType type,
                                               final String title, final String content,
                                               final Long targetId, final String redirectUrl) {
        return memberIds.stream()
                .map(memberId -> toNotification(memberId, type, title, content, targetId, redirectUrl))
                .toList();
    }

    private Notification toNotification(final Long memberId, final NotificationType type,
                                        final String title, final String content,
                                        final Long targetId, final String redirectUrl) {
        return Notification.builder()
                .memberId(memberId)
                .type(type)
                .title(title)
                .content(content)
                .targetId(targetId)
                .redirectUrl(redirectUrl)
                .build();
    }
}
