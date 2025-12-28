package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamCommentUpdateRequest(
	@NotBlank(message = "수정할 댓글 내용은 비어 있을 수 없습니다.")
	String description
) {}
