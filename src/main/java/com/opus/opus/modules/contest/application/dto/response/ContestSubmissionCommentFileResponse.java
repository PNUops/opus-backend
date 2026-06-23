package com.opus.opus.modules.contest.application.dto.response;

public record ContestSubmissionCommentFileResponse(

        Long fileId,
        String fileName,
        Long fileSize
) {
}
