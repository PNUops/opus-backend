package com.opus.opus.modules.file.application.dto;

import org.springframework.core.io.Resource;

public record FileDownload(Resource resource, String fileName, String mimeType) {
}
