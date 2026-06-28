package com.opus.opus.modules.contest.domain;

import java.util.Optional;

public enum SubmissionFileFormat {

    PDF,
    ZIP,
    PNG,
    JPG,
    JPEG,
    GIF,
    MP4,
    PPT,
    PPTX,
    DOC,
    DOCX,
    HWP;

    public static Optional<SubmissionFileFormat> fromExtension(final String extension) {
        if (extension == null || extension.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(SubmissionFileFormat.valueOf(extension.toUpperCase()));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
