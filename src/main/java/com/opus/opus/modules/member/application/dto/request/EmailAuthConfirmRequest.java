package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record EmailAuthConfirmRequest(

        @NotNull(message = "이메일을 입력해주세요.")
        String email,

        @NotNull(message = "인증코드를 입력해주세요.")
        String authCode
) {
}
