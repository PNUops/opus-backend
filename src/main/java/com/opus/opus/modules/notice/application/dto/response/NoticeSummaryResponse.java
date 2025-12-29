package com.opus.opus.modules.notice.application.dto.response;

import com.opus.opus.modules.notice.domain.Notice;
import java.time.LocalDateTime;

public record NoticeSummaryResponse(

        Long noticeId,
        String title,
        LocalDateTime createdAt
) {

    public static NoticeSummaryResponse from(final Notice notice) {
        return new NoticeSummaryResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt()
        );
    }
}
