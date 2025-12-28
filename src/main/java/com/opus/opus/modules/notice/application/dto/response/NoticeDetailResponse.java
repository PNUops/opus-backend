package com.opus.opus.modules.notice.application.dto.response;

import com.opus.opus.modules.notice.domain.Notice;
import java.time.LocalDateTime;

public record NoticeDetailResponse(

        String title,

        String description,

        LocalDateTime updatedAt,

        LocalDateTime createdAt
) {

    public static NoticeDetailResponse from(final Notice notice) {
        return new NoticeDetailResponse(
                notice.getTitle(),
                notice.getDescription(),
                notice.getUpdatedAt(),
                notice.getCreatedAt()
        );
    }
}
