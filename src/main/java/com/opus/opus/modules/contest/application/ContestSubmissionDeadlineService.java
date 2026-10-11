package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineNotificationEvent;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineTeam;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ContestSubmissionDeadlineService {

    private static final List<Integer> NOTIFICATION_DAYS_LEFT = List.of(3, 1);

    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ApplicationEventPublisher eventPublisher;

    public void publishDeadlineNotifications(final LocalDate today) {
        NOTIFICATION_DAYS_LEFT.forEach(daysLeft -> publishDeadlineNotificationsByDaysLeft(today, daysLeft));
    }

    private void publishDeadlineNotificationsByDaysLeft(final LocalDate today, final int daysLeft) {
        final List<ContestSubmissionItem> submissionItems =
                contestSubmissionItemConvenience.findAllByDeadlineDate(today.plusDays(daysLeft));
        log.info("제출 마감 D-{} 알림 대상 제출 항목 {}건", daysLeft, submissionItems.size());
        submissionItems.forEach(submissionItem -> publishDeadlineNotification(submissionItem, daysLeft));
    }

    private void publishDeadlineNotification(final ContestSubmissionItem submissionItem, final int daysLeft) {
        final List<SubmissionDeadlineTeam> teams = findNotSubmittedTeams(submissionItem);
        if (teams.isEmpty()) {
            return;
        }
        eventPublisher.publishEvent(new SubmissionDeadlineNotificationEvent(submissionItem.getContest().getId(),
                submissionItem.getId(), submissionItem.getName(), daysLeft, teams));
    }

    private List<SubmissionDeadlineTeam> findNotSubmittedTeams(final ContestSubmissionItem submissionItem) {
        final List<Long> teamIds = contestSubmissionConvenience.findNotSubmittedTeamIds(submissionItem);
        if (teamIds.isEmpty()) {
            return List.of();
        }
        return teamMemberConvenience.findRealMemberIdsByTeamIds(teamIds).entrySet().stream()
                .map(entry -> new SubmissionDeadlineTeam(entry.getKey(), entry.getValue()))
                .toList();
    }
}
