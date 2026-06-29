package com.opus.opus.modules.contest.application.dto.response;

import java.util.List;

public record MentorProjectsResponse(

        int assignedTeamCount,
        long pendingFeedbackCount,
        List<MentorProjectResponse> projects
) {
}
