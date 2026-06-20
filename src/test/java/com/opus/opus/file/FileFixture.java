package com.opus.opus.file;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;

public class FileFixture {

    public static File createFile(final String name, final String path) {
        return File.create(name, path, "image/webp", 0L);
    }

    public static FileImage createFileImage(final File file, final Long referenceId,
                                            final ReferenceDomainType referenceType,
                                            final FileImageType imageType) {
        return FileImage.builder()
                .file(file)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .imageType(imageType)
                .build();
    }

    public static FileImage createTeamPosterFileImage(final Long teamId) {
        final File file = File.create("poster.jpg", "path/to/poster.webp", "image/webp", 0L);
        return createFileImage(file, teamId, TEAM, POSTER);
    }

    public static FileImage createTeamPosterFileImage() {
        return createTeamPosterFileImage(1L);
    }

    public static FileImage createTrackThumbnailFileImage(final Long trackId) {
        final File file = File.create("track_thumbnail.jpg", "path/to/track_thumbnail.webp", "image/webp", 0L);
        return createFileImage(file, trackId, TRACK, THUMBNAIL);
    }

    public static FileImage createTeamThumbnailFileImage(final Long teamId) {
        final File file = File.create("team_thumbnail.jpg", "path/to/team_thumbnail.webp", "image/webp", 0L);
        return createFileImage(file, teamId, TEAM, THUMBNAIL);
    }

    public static FileImage createMemberProfileFileImage(final Long memberId) {
        final File file = File.create("profile.jpg", "path/to/profile.webp", "image/webp", 0L);
        return createFileImage(file, memberId, MEMBER, PROFILE);
    }

    public static FileImage createTeamPreviewFileImage(final Long teamId) {
        final File file = File.create("preview.jpg", "path/to/preview.webp", "image/webp", 0L);
        return createFileImage(file, teamId, TEAM, PREVIEW);
    }
}
