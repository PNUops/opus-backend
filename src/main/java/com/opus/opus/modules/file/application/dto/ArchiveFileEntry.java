package com.opus.opus.modules.file.application.dto;

public record ArchiveFileEntry(

        Long submissionId,
        String fileName,
        Long fileSize,
        String filePath
) {
}
