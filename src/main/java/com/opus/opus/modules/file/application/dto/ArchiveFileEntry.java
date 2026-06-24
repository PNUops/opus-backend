package com.opus.opus.modules.file.application.dto;

public record ArchiveFileEntry(

        Long submissionId,
        Long fileDocumentId,
        String fileName,
        Long fileSize
) {
}
