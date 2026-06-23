package com.opus.opus.modules.contest.application.dto.request;

import java.util.List;

public record ContestSubmissionCommentUpdateRequest(

        String description,
        List<Long> removeFileIds
) {
}
