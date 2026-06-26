package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import java.time.LocalDateTime;
import java.util.Set;

public class ContestSubmissionItemFixture {

    public static ContestSubmissionItem createSubmissionItem(final Contest contest) {
        return ContestSubmissionItem.builder()
                .name("테스트 제출 항목")
                .description("테스트 설명")
                .allowedFileFormats(Set.of(SubmissionFileFormat.PDF))
                .maxFileSizeMb(10)
                .maxFileCount(5)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .allowLateSubmission(false)
                .visibility(SubmissionVisibility.PUBLIC)
                .contest(contest)
                .build();
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
