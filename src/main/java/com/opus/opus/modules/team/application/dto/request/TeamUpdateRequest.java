package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.Pattern;

public record TeamUpdateRequest(
        Long contestId,

        Long trackId,

        String projectName,

        String teamName,

        String professorName,

        @Pattern(
                regexp = "^https://github\\.com/.+",
                message = "올바른 GitHub 주소 형식이 아닙니다."
        )
        String githubPath,

        @Pattern(
                regexp = "^(https://(www\\.)?youtube\\.com/.+|https://youtu\\.be/.+)$",
                message = "올바른 YouTube 주소 형식이 아닙니다."
        )
        String youTubePath,

        String productionPath,

        String overview
) {
}
