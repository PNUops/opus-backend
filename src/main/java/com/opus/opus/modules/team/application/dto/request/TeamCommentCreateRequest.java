package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamCommentCreateRequest(
        @NotBlank(message = "작성할 댓글 내용은 필수입니다.")
        String description
) {}
