package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record TeamVoteToggleRequest(
        @NotNull(message = "isVoted 값은 필수입니다.")
        Boolean isVoted
) {
}
