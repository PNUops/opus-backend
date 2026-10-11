package com.opus.opus.modules.contest.application;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.submission-deadline.enabled", havingValue = "true", matchIfMissing = true)
public class SubmissionDeadlineNotificationScheduler {

    private static final String ZONE = "Asia/Seoul";

    private final ContestSubmissionDeadlineService contestSubmissionDeadlineService;

    @Scheduled(cron = "0 0 9 * * *", zone = ZONE)
    public void publishDeadlineNotifications() {
        contestSubmissionDeadlineService.publishDeadlineNotifications(LocalDate.now(ZoneId.of(ZONE)));
    }
}
