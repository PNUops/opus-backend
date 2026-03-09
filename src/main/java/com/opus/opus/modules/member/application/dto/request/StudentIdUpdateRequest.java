package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StudentIdUpdateRequest(

        @NotBlank(message = "학번을 입력해주세요.")
        @Pattern(regexp = "^\\d{9}$", message = "학번은 9자리 숫자여야 합니다.")
        String studentId
) {
}
