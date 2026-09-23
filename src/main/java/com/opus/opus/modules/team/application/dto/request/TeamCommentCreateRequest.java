package com.opus.opus.modules.team.application.dto.request;

import com.opus.opus.modules.team.domain.TeamCommentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCommentCreateRequest(

        @NotBlank(message = "작성할 댓글 내용은 필수입니다.")
        @Size(max = 3000, message = "댓글은 최대 3000자까지 작성할 수 있습니다.")
        String description,

        TeamCommentVisibility visibility
) {}
