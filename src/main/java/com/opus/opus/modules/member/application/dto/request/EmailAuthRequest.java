package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record EmailAuthRequest(

        @NotNull(message = "이메일 입력해주세요.")
        String email
) {
}
