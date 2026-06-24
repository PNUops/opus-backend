package com.opus.opus.modules.file.application.dto;

import org.springframework.core.io.Resource;

public record DocumentFileDownload(
        Resource resource,
        String fileName,
        String mimeType,
        Long fileSize,
        Long submissionId
) {
}
