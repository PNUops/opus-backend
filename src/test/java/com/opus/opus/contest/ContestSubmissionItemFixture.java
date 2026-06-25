package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionItemMemo;
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

    public static ContestSubmissionItemMemo createMemo(final ContestSubmissionItem submissionItem) {
        return ContestSubmissionItemMemo.builder()
                .content("테스트 메모 내용입니다.")
                .submissionItem(submissionItem)
                .build();
    }
}
