package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StudentIdUpdateRequest(

        @NotBlank(message = "학번을 입력해주세요.")
        String studentId
) {
}