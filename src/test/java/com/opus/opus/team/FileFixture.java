package com.opus.opus.team;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;

import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;

import com.opus.opus.modules.file.domain.File;

public class FileFixture {

    public static File createTeamPosterFile() {
        return createTeamPosterFile(1L);
    }

    public static File createTeamPosterFile(final Long teamId) {
        return File.builder()
                .name("poster.jpg")
                .filePath("path/to/poster.webp")
                .referenceId(teamId)
                .referenceType(TEAM)
                .imageType(POSTER)
                .build();
    }

    public static File createTrackThumbnailFile(final Long trackId) {
        return File.builder()
                .name("track_thumbnail.jpg")
                .filePath("path/to/track_thumbnail.webp")
                .referenceId(trackId)
                .referenceType(TRACK)
                .imageType(THUMBNAIL)
                .build();
    }

    public static File createTeamThumbnailFile(final Long teamId) {
        return File.builder()
                .name("team_thumbnail.jpg")
                .filePath("path/to/team_thumbnail.webp")
                .referenceId(teamId)
                .referenceType(TEAM)
                .imageType(THUMBNAIL)
                .build();
    }

    public static File createTeamPreviewFile(final Long teamId) {
        return File.builder()
                .name("preview.jpg")
                .filePath("path/to/preview.webp")
                .referenceId(teamId)
                .referenceType(TEAM)
                .imageType(PREVIEW)
                .build();
    }
}
