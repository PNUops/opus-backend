package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ContestSubmissionCommentCreateRequest(

        @NotBlank(message = "코멘트 본문은 필수입니다.")
        String description
) {
}
