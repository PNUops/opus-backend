package com.opus.opus.modules.member.application.dto.request;

import com.opus.opus.modules.member.domain.MemberRoleType;
import jakarta.validation.constraints.NotNull;

public record MemberRoleChangeRequest(

        MemberRoleType currentRole,

        @NotNull(message = "변경할 역할을 선택해주세요.")
        MemberRoleType newRole
) {
}
