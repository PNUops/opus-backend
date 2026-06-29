package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.file.domain.FileDocument;

public record FileResponse(

        Long fileId,
        String fileName,
        Long fileSize
) {
    public static FileResponse from(final FileDocument fileDocument) {
        return new FileResponse(fileDocument.getId(), fileDocument.getName(), fileDocument.getFileSize());
    }
}
