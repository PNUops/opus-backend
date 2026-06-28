package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import com.opus.opus.modules.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionFeedbackResponse(

        Long feedbackId,
        Long memberId,
        String memberName,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ContestSubmissionFeedbackFileResponse> files
) {
    public static ContestSubmissionFeedbackResponse of(final ContestSubmissionFeedback feedback, final Member member, final List<FeedbackFileInfo> files) {
        return new ContestSubmissionFeedbackResponse(
                feedback.getId(),
                feedback.getMemberId(),
                member != null ? member.getName() : null,
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
