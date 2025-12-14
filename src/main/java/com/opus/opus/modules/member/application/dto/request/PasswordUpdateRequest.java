package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record PasswordUpdateRequest(

        @NotNull(message = "가입한 이메일을 입력해주세요.")
        String email,

        @NotNull(message = "새로운 비밀번호를 입력해주세요.")
        String newPassword
) {
}
