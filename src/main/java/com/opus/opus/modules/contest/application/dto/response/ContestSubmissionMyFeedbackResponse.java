package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionMyFeedbackResponse(

        Long feedbackId,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ContestSubmissionFeedbackFileResponse> files
) {
    public static ContestSubmissionMyFeedbackResponse of(final ContestSubmissionFeedback feedback, final List<FeedbackFileInfo> files) {
        return new ContestSubmissionMyFeedbackResponse(
                feedback.getId(),
                feedback.getDescription(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt(),
                files.stream()
                        .map(file -> new ContestSubmissionFeedbackFileResponse(
                                file.fileId(), file.fileName(), file.fileSize()))
                        .toList()
        );
    }
}
