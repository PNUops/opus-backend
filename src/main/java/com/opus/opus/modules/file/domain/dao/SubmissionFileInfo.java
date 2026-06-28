package com.opus.opus.modules.file.domain.dao;

public record SubmissionFileInfo(

        Long submissionId,
        Long fileDocumentId,
        String fileName
) {
}
