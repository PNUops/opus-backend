package com.opus.opus.modules.team.application.dto.response;

public record TeamLikeToggleResponse(
        Long teamId,
        Boolean isLiked,
        String message
) {
    public static TeamLikeToggleResponse of(Long teamId, Boolean isLiked, String message) {
        return new TeamLikeToggleResponse(teamId, isLiked, message);
    }
}
