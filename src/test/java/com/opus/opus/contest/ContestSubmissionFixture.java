package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import java.time.LocalDateTime;
import java.util.Set;

public class ContestSubmissionFixture {

    public static ContestSubmissionItem createSubmissionItem(final Contest contest) {
        return ContestSubmissionItem.builder()
                .name("중간발표 자료")
                .maxFileSizeMb(10)
                .maxFileCount(5)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .allowLateSubmission(false)
                .visibility(SubmissionVisibility.PUBLIC)
                .allowedFileFormats(Set.of(SubmissionFileFormat.PDF))
                .contest(contest)
                .build();
    }

    public static ContestSubmission createSubmission(final Long teamId, final ContestSubmissionItem submissionItem) {
        return ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(LocalDateTime.now())
                .submissionItem(submissionItem)
                .build();
    }
}
