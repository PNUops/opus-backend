package com.opus.opus.modules.contest.application.dto.response;

public record ContestCurrentToggleResponse(
        Long contestId,
        Boolean isCurrent,
        String message
) {
}
