package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamCommentUpdateRequest(

        @NotBlank(message = "수정할 댓글 내용은 비어 있을 수 없습니다.")
        @Size(max = 3000, message = "댓글은 최대 3000자까지 작성할 수 있습니다.")
        String description
) {
}
