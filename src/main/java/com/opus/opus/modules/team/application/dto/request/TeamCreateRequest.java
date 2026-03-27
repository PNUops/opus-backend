package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(

        @NotNull(message = "contestId는 필수 값입니다.")
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

        @Size(max = 3000, message = "overview는 최대 3000자까지 가능합니다.")
        String overview
) {
}
