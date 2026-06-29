package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.SubmissionFileInfo;

public record ContestSubmissionFileResponse(

        Long fileId,
        String fileName,
        Long fileSize
) {
    public static ContestSubmissionFileResponse from(final FileDocument fileDocument) {
        return new ContestSubmissionFileResponse(fileDocument.getId(), fileDocument.getName(), fileDocument.getFileSize());
    }

    public static ContestSubmissionFileResponse from(final SubmissionFileInfo fileInfo) {
        return new ContestSubmissionFileResponse(fileInfo.fileDocumentId(), fileInfo.fileName(), fileInfo.fileSize());
    }
}
