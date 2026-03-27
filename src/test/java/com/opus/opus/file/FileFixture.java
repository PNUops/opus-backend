package com.opus.opus.file;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;

import com.opus.opus.modules.file.domain.File;

public class FileFixture {

    public static File createTeamPosterFile() {
        return File.builder()
                .name("poster.jpg")
                .filePath("path/to/poster.webp")
                .referenceId(1L)
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

    public static File createMemberProfileFile(final Long memberId) {
        return File.builder()
                .name("profile.jpg")
                .filePath("path/to/profile.webp")
                .referenceId(memberId)
                .referenceType(MEMBER)
                .imageType(PROFILE)
                .build();
    }
}
