package com.opus.opus.modules.contest.application.dto.response;

public record ContestSubmissionFeedbackFileResponse(

        Long fileId,
        String fileName,
        Long fileSize
) {
}
