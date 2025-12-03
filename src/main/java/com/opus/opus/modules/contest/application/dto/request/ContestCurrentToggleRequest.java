package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContestCurrentToggleRequest(
        @NotNull
        Boolean isCurrent
) {
}
