package com.opus.opus.modules.contest.application.dto.response;

public record ContestCurrentToggleResponse(
        Long contestId,
        boolean isCurrent,
        String message
) {
    public static ContestCurrentToggleResponse of(Long contestId, boolean isCurrent) {
        String message = isCurrent
                ? "현재 대회로 등록되었습니다."
                : "현재 대회 지정이 해제되었습니다.";

        return new ContestCurrentToggleResponse(contestId, isCurrent, message);
    }
}
