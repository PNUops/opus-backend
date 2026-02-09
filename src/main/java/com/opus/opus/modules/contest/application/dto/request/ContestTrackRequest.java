package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContestTrackRequest(

        @NotBlank(message = "분과명은 비어 있을 수 없습니다.")
        String trackName
) {
}
