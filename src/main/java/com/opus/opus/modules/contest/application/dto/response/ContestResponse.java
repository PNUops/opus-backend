package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record ContestResponse(
        Long contestId,
        String contestName,
        Long categoryId,
        String categoryName,
        Boolean isCurrent,
        LocalDateTime updatedAt
) {
}
