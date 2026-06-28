package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionItemResponse(

        String name,
        Long contestTrackId,
        String description,
        List<String> allowedFileFormats,
        Integer maxFileSizeMb,
        Integer maxFileCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Boolean allowLateSubmission,
        String visibility
) {

    public static ContestSubmissionItemResponse from(final ContestSubmissionItem submissionItem) {
        final ContestTrack contestTrack = submissionItem.getContestTrack();
        return new ContestSubmissionItemResponse(
                submissionItem.getName(),
                contestTrack != null ? contestTrack.getId() : null,
                submissionItem.getDescription(),
                submissionItem.getAllowedFileFormats().stream().map(Enum::name).toList(),
                submissionItem.getMaxFileSizeMb(),
                submissionItem.getMaxFileCount(),
                submissionItem.getStartAt(),
                submissionItem.getEndAt(),
                submissionItem.getAllowLateSubmission(),
                submissionItem.getVisibility().name()
        );
    }
}
