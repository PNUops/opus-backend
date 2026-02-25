package com.opus.opus.modules.contest.application.dto.response;

import java.time.LocalDateTime;

public record ContestVoteLogResponse(

        String voterName,
        String voterEmail,
        String teamName,
        LocalDateTime votedAt
) {
}
