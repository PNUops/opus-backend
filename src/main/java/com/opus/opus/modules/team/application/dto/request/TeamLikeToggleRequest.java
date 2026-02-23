package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record TeamLikeToggleRequest(
        @NotNull(message = "isLiked 값은 필수입니다.")
        Boolean isLiked
) {
}
