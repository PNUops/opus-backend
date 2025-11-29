package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.Contest;
import java.time.LocalDateTime;

public record ContestResponse(
        Long contestId,
        String contestName,
        Long categoryId,
        String categoryName,
        Boolean isCurrent,
        LocalDateTime updatedAt
) {
    public static ContestResponse from(Contest contest, String categoryName) {
        return new ContestResponse(
                contest.getId(),
                contest.getContestName(),
                contest.getCategoryId(),
                categoryName,
                contest.getIsCurrent(),
                contest.getUpdatedAt()
        );
    }
}
