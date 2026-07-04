package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.Contest;
import java.util.List;

public record MentorContestResponse(

        Long contestId,
        String contestName,
        String categoryName,
        List<String> assignedTrackNames,
        long totalPendingFeedbackCount,
        int totalAssignedTeamCount
) {
    public static MentorContestResponse of(final Contest contest, final String categoryName,
                                           final List<String> assignedTrackNames,
                                           final long totalPendingFeedbackCount, final int totalAssignedTeamCount) {
        return new MentorContestResponse(
                contest.getId(),
                contest.getContestName(),
                categoryName,
                assignedTrackNames,
                totalPendingFeedbackCount,
                totalAssignedTeamCount
        );
    }
}
