package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionStatusResult;
import java.time.LocalDateTime;

public record ContestSubmissionStatusResponse(

        Long submissionId,
        Long teamId,
        String teamName,
        String trackName,
        String submissionItemName,
        SubmissionStatus status,
        LocalDateTime firstSubmittedAt,
        LocalDateTime lastModifiedAt
) {
    public static ContestSubmissionStatusResponse of(final ContestSubmissionStatusResult result,
                                                     final SubmissionStatus status) {
        return new ContestSubmissionStatusResponse(
                result.submissionId(),
                result.teamId(),
                result.teamName(),
                result.trackName(),
                result.submissionItemName(),
                status,
                result.firstSubmittedAt(),
                result.lastModifiedAt()
        );
    }
}
