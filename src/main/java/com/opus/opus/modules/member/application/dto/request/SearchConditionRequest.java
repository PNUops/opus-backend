package com.opus.opus.modules.member.application.dto.request;

import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;

public record SearchConditionRequest(
        Pageable pageable,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
