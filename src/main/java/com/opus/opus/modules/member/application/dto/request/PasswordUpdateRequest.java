package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateRequest(

        @NotNull(message = "가입한 이메일을 입력해주세요.")
        String email,

        @NotNull(message = "새로운 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&~])[A-Za-z\\d@$!%*#?&~]{8,16}$",
                message = "비밀번호는 8~16자여야 하며, 영문·숫자·특수문자를 모두 포함해야 합니다."
        )
        String newPassword
) {
}
