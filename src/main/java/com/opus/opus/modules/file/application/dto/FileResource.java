package com.opus.opus.modules.file.application.dto;

import org.springframework.core.io.Resource;

public record FileResource(Resource resource, String mimeType) {
}
