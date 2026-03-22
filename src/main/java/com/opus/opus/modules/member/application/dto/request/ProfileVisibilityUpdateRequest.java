package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProfileVisibilityUpdateRequest(

        @NotNull(message = "프로필 공개 여부를 입력해주세요.")
        Boolean isProfilePublic
) {
}
