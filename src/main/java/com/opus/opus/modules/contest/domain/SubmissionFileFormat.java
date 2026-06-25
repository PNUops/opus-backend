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

    public static Optional<SubmissionFileFormat> from(final String filename) {
        final String extension = extractExtension(filename);
        if (extension == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SubmissionFileFormat.valueOf(extension.toUpperCase()));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String extractExtension(final String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1);
    }
}
