package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(

        @NotNull(message = "이름을 입력해주세요.")
        String name,

        @NotNull(message = "학번을 입력해주세요.")
        String studentId,

        @NotNull(message = "부산대 이메일을 입력해주세요.")
        String email,

        @NotNull(message = "비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&~])[A-Za-z\\d@$!%*#?&~]{8,16}$",
                message = "비밀번호는 8~16자여야 하며, 영문·숫자·특수문자를 모두 포함해야 합니다."
        )
        String password,

        @NotNull(message = "회원 유형을 입력해주세요.")
        @Pattern(
                regexp = "STUDENT|STAFF",
                message = "회원 유형은 STUDENT 또는 STAFF만 가능합니다."
        )
        String memberType
) {
}
