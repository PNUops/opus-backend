package com.opus.opus.contest;

import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.PDF;
import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.ZIP;
import static com.opus.opus.modules.contest.domain.SubmissionVisibility.PUBLIC;

import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContestSubmissionItemFixture {

    public static ContestSubmissionItem createSubmissionItem(final Contest contest) {
        return baseBuilder(contest).build();
    }

    public static ContestSubmissionItem createSubmissionItem(final Contest contest, final ContestTrack contestTrack) {
        return createSubmissionItem(contest, contestTrack, "발표자료",
                LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59));
    }

    public static ContestSubmissionItem createSubmissionItem(final Contest contest, final ContestTrack contestTrack,
                                                             final String name, final LocalDateTime startAt,
                                                             final LocalDateTime endAt) {
        return ContestSubmissionItem.builder()
                .name(name)
                .description("PDF 형식의 발표자료를 제출하세요.")
                .allowedFileFormats(Set.of(PDF, ZIP))
                .maxFileSizeMb(50)
                .maxFileCount(3)
                .startAt(startAt)
                .endAt(endAt)
                .allowLateSubmission(true)
                .visibility(PUBLIC)
                .contest(contest)
                .contestTrack(contestTrack)
                .build();
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

    public static ContestSubmissionItem createSubmissionItemWithVisibility(final Contest contest,
                                                                           final SubmissionVisibility visibility) {
        return baseBuilder(contest).visibility(visibility).build();
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

    public static ContestSubmissionItemRequest createRequest(final Long contestTrackId) {
        return new ContestSubmissionItemRequest(
                "발표자료",
                contestTrackId,
                "PDF 형식의 발표자료를 제출하세요.",
                List.of(PDF, ZIP),
                50,
                3,
                LocalDateTime.of(2026, 7, 1, 0, 0),
                LocalDateTime.of(2026, 7, 31, 23, 59),
                true,
                PUBLIC
        );
    }

    public static ContestSubmissionItemRequest createRequestWithPeriod(final LocalDateTime startAt,
                                                                       final LocalDateTime endAt) {
        return new ContestSubmissionItemRequest(
                "발표자료",
                null,
                "PDF 형식의 발표자료를 제출하세요.",
                List.of(PDF),
                50,
                3,
                startAt,
                endAt,
                true,
                PUBLIC
        );
    }

    public static ContestSubmission createSubmission(final ContestSubmissionItem submissionItem,
                                                     final Long teamId) {
        return ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(LocalDateTime.now())
                .submissionItem(submissionItem)
                .build();
    }

    public static ContestSubmissionMemo createMemo(final ContestSubmission submission) {
        return ContestSubmissionMemo.builder()
                .content("테스트 메모 내용입니다.")
                .submission(submission)
                .build();
    }
}
