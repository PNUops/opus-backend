package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.UpcomingSubmissionResult;
import java.time.LocalDateTime;

public record UpcomingSubmissionResponse(

        Long submissionItemId,
        String submissionItemName,
        LocalDateTime deadlineAt,
        LocalDateTime lastModifiedAt,
        SubmissionStatus status
) {
    public static UpcomingSubmissionResponse of(final UpcomingSubmissionResult result, final SubmissionStatus status) {
        return new UpcomingSubmissionResponse(
                result.submissionItemId(),
                result.submissionItemName(),
                result.deadlineAt(),
                result.lastModifiedAt(),
                status
        );
    }
}
