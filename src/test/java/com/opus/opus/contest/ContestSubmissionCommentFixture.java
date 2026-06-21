package com.opus.opus.contest;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionComment;

public class ContestSubmissionCommentFixture {

    public static final String COMMENT_DESCRIPTION = "테스트용 코멘트입니다.";

    public static ContestSubmissionComment createComment(final ContestSubmission submission, final Long memberId) {
        return ContestSubmissionComment.builder()
                .description(COMMENT_DESCRIPTION)
                .memberId(memberId)
                .submission(submission)
                .build();
    }
}
