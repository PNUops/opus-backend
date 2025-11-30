package com.opus.opus.modules.contest.application.dto.response;

public record ContestCurrentToggleResponse(
        Long contestId,
        Boolean isCurrent,
        String message
) {
    public static ContestCurrentToggleResponse of(Long id, Boolean isCurrent, String message) {
        return new ContestCurrentToggleResponse(id, isCurrent, message);
    }
}
