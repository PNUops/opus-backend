package com.opus.opus.modules.team.domain.dao;

import java.time.LocalDateTime;

public record VoteRecordResult(
        Long memberId,
        LocalDateTime votedAt
) {
}
