package com.opus.opus.modules.contest.application.dto.response;

public record VoterRoleResponse(

        String role,
        Long voterCount,
        Double ratio
) {
}
