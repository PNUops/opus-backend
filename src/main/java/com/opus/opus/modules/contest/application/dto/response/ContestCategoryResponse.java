package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record ContestCategoryResponse(
        Long categoryId,
        String categoryName,
        LocalDateTime updatedAt
) {
}
