package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContestSubmissionItemMemoRequest(

        @NotBlank(message = "메모 내용을 입력해주세요.")
        @Size(max = 500, message = "메모는 500자 이하로 입력해주세요.")
        String content
) {
}