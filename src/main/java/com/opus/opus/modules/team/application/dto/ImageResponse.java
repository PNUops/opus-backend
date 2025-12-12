package com.opus.opus.modules.team.application.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ImageResponse(
        Resource resource,
        String contentType
) {
    public MediaType getMediaType() {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.IMAGE_JPEG; // 기본값 설정
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.IMAGE_JPEG; // 파싱 실패 시 기본값
        }
    }
}
