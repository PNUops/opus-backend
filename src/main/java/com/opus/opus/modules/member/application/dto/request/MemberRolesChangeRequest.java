package com.opus.opus.modules.member.application.dto.request;

import com.opus.opus.modules.member.domain.MemberRoleType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record MemberRolesChangeRequest(

        @NotNull(message = "현재 역할을 입력해주세요.")
        Set<@NotNull MemberRoleType> currentRoles,

        @NotEmpty(message = "변경할 역할을 하나 이상 선택해주세요.")
        Set<@NotNull MemberRoleType> roles,

        boolean releaseAssignments
) {
}
