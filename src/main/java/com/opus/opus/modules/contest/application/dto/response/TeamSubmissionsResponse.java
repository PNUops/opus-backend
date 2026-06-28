package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse.FeedbackStatus;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record TeamSubmissionsResponse(

        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        long pendingFeedbackCount,
        List<MentorSubmissionResponse> submissions
) {
    public static TeamSubmissionsResponse of(final Team team, final String trackName,
                                             final List<MentorSubmissionResponse> submissions) {
        final long pendingFeedbackCount = submissions.stream()
                .filter(submission -> submission.feedbackStatus() == FeedbackStatus.PENDING)
                .count();
        return new TeamSubmissionsResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                trackName,
                pendingFeedbackCount,
                submissions
        );
    }
}
