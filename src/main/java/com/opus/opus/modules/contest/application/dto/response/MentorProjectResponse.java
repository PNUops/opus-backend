package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.domain.Team;

public record MentorProjectResponse(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        String roleType,
        long pendingFeedbackCount
) {
    public static MentorProjectResponse of(final Team team, final String trackName, final String roleType,
                                           final long pendingFeedbackCount) {
        return new MentorProjectResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                trackName,
                roleType,
                pendingFeedbackCount
        );
    }
}
