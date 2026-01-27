package com.opus.opus.team;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;

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
}
