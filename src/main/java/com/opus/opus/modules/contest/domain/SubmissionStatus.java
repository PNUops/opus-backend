package com.opus.opus.modules.contest.domain;

import java.time.LocalDateTime;

public enum SubmissionStatus {

    SUBMITTED,
    LATE,
    NOT_SUBMITTED,
    NOT_SUBMITTED_AFTER_DEADLINE;

    public static SubmissionStatus from(final LocalDateTime firstSubmittedAt, final LocalDateTime deadlineAt) {
        return firstSubmittedAt.isAfter(deadlineAt) ? LATE : SUBMITTED;
    }
}
