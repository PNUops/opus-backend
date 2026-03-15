package com.opus.opus.modules.team.application.dto.response;

import com.opus.opus.modules.team.domain.Team;

public record TeamCreateResponse(
        Long teamId
) {
    public static TeamCreateResponse from(final Team team) {
        return new TeamCreateResponse(team.getId());
    }
}
