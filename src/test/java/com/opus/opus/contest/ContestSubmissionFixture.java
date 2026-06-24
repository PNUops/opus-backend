package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ContestSubmissionFixture {

    public static ContestSubmissionItem createSubmissionItem(final Contest contest) {
        return createSubmissionItem(contest, null);
    }

    public static ContestSubmissionItem createSubmissionItem(final Contest contest, final ContestTrack track) {
        return ContestSubmissionItem.builder()
                .name("최종 발표 자료")
                .description("최종 발표 자료를 제출해주세요.")
                .allowedFileFormats(new HashSet<>(Set.of(SubmissionFileFormat.PDF, SubmissionFileFormat.PPTX)))
                .maxFileSizeMb(100)
                .maxFileCount(5)
                .startAt(LocalDateTime.of(2026, 6, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 6, 30, 0, 0))
                .allowLateSubmission(true)
                .visibility(SubmissionVisibility.PUBLIC)
                .contest(contest)
                .contestTrack(track)
                .build();
    }

    public static ContestSubmission createSubmission(final Long teamId, final ContestSubmissionItem submissionItem) {
        return ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(LocalDateTime.of(2026, 6, 5, 12, 0))
                .submissionItem(submissionItem)
                .build();
    }
}
