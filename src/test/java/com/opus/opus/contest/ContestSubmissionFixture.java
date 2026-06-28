package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.time.LocalDateTime;

public class ContestSubmissionFixture {

    public static ContestSubmission createSubmission(final Long teamId, final ContestSubmissionItem submissionItem) {
        return ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(LocalDateTime.now())
                .submissionItem(submissionItem)
                .build();
    }

    public static ContestSubmission createSubmissionWithFirstSubmittedAt(final Long teamId,
                                                                         final ContestSubmissionItem submissionItem,
                                                                         final LocalDateTime firstSubmittedAt) {
        return ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(firstSubmittedAt)
                .submissionItem(submissionItem)
                .build();
    }
}
