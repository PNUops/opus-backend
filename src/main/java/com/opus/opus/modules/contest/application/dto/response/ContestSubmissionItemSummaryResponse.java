package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import java.time.LocalDateTime;

public record ContestSubmissionItemSummaryResponse(

        Long contestSubmissionItemId,
        String name,
        String trackName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean allowLateSubmission,
        String visibility,
        String operationStatus
) {

    public static ContestSubmissionItemSummaryResponse of(final ContestSubmissionItem submissionItem,
                                                          final String operationStatus) {
        final ContestTrack contestTrack = submissionItem.getContestTrack();
        return new ContestSubmissionItemSummaryResponse(
                submissionItem.getId(),
                submissionItem.getName(),
                contestTrack != null ? contestTrack.getTrackName() : null,
                submissionItem.getStartAt(),
                submissionItem.getEndAt(),
                submissionItem.getAllowLateSubmission(),
                submissionItem.getVisibility().name(),
                operationStatus
        );
    }
}
