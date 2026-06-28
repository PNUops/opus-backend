package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ContestSubmissionItemFixture {

    public static ContestSubmissionItem createSubmissionItem(final Contest contest) {
        return baseBuilder(contest).build();
    }

    public static ContestSubmissionItem createSubmissionItem(final Contest contest, final ContestTrack track) {
        return baseBuilder(contest).contestTrack(track).build();
    }

    public static ContestSubmissionItem createSubmissionItemWithDeadline(final Contest contest,
                                                                         final LocalDateTime endAt,
                                                                         final boolean allowLateSubmission) {
        return baseBuilder(contest)
                .startAt(endAt.minusDays(7))
                .endAt(endAt)
                .allowLateSubmission(allowLateSubmission)
                .build();
    }

    public static ContestSubmissionItem createSubmissionItemWithMaxFileCount(final Contest contest,
                                                                             final int maxFileCount) {
        return baseBuilder(contest).maxFileCount(maxFileCount).build();
    }

    public static ContestSubmissionItem createSubmissionItemWithMaxFileSizeMb(final Contest contest,
                                                                              final int maxFileSizeMb) {
        return baseBuilder(contest).maxFileSizeMb(maxFileSizeMb).build();
    }

    private static ContestSubmissionItem.ContestSubmissionItemBuilder baseBuilder(final Contest contest) {
        return ContestSubmissionItem.builder()
                .name("최종 발표 자료")
                .description("발표 자료를 제출하세요")
                .allowedFileFormats(new HashSet<>(Set.of(SubmissionFileFormat.PDF, SubmissionFileFormat.MP4)))
                .maxFileSizeMb(50)
                .maxFileCount(5)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .allowLateSubmission(false)
                .visibility(SubmissionVisibility.PUBLIC)
                .contest(contest);
    }
}
