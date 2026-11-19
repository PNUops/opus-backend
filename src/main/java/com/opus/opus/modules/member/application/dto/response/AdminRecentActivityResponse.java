package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.AdminActivityHistory;
import com.opus.opus.modules.member.domain.AdminActivityTargetType;
import com.opus.opus.modules.member.domain.AdminActivityType;
import java.time.LocalDateTime;

public record AdminRecentActivityResponse(

        Long id,

        AdminActivityType type,

        String actorName,

        AdminActivityTargetType targetType,

        String targetName,

        Long contestId,

        String contestName,

        String summary,

        LocalDateTime createdAt
) {

    // 서비스 전역 작업이거나 대회가 삭제되었으면 contestName은 null이다.
    public static AdminRecentActivityResponse of(final AdminActivityHistory history, final String contestName) {
        return new AdminRecentActivityResponse(
                history.getId(),
                history.getType(),
                history.getActorName(),
                history.getTargetType(),
                history.getTargetName(),
                history.getContestId(),
                contestName,
                history.getSummary(),
                history.getCreatedAt()
        );
    }
}
