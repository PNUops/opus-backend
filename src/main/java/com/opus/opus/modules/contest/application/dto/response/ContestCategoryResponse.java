package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestCategory;
import java.time.LocalDateTime;

public record ContestCategoryResponse(

        Long categoryId,

        String categoryName,

        LocalDateTime updatedAt

) {
    public static ContestCategoryResponse from(ContestCategory contestCategory) {
        return new ContestCategoryResponse(
                contestCategory.getId(),
                contestCategory.getCategoryName(),
                contestCategory.getUpdatedAt()
        );
    }
}
