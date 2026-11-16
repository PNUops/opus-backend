package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminPermissionChangeRequest(

        @NotNull(message = "관리 권한 부여 여부를 입력해주세요.")
        Boolean isAdmin
) {
}
