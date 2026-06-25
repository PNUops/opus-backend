package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionItemMemo;
import java.time.LocalDateTime;

public record ContestSubmissionItemMemoResponse(
        Long memoId,
        String content,
        LocalDateTime updatedAt
) {
    public static ContestSubmissionItemMemoResponse from(final ContestSubmissionItemMemo memo) {
        return new ContestSubmissionItemMemoResponse(
                memo.getId(),
                memo.getContent(),
                memo.getUpdatedAt()
        );
    }
}