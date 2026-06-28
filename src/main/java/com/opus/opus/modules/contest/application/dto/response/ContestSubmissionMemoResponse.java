package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import java.time.LocalDateTime;

public record ContestSubmissionMemoResponse(
        Long memoId,
        String content,
        LocalDateTime updatedAt
) {
    public static ContestSubmissionMemoResponse from(final ContestSubmissionMemo memo) {
        return new ContestSubmissionMemoResponse(
                memo.getId(),
                memo.getContent(),
                memo.getUpdatedAt()
        );
    }
}
