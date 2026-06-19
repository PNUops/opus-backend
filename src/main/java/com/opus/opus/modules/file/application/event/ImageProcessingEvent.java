package com.opus.opus.modules.file.application.event;

public record ImageProcessingEvent(
        byte[] imageBytes,
        String relativePath,
        Long fileImageId
) {}
