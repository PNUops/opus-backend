package com.opus.opus.modules.invitation.application.dto.request;

import com.opus.opus.modules.member.domain.MemberRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InvitationCreateRequest(

        @NotNull(message = "대회 ID는 필수입니다.")
        Long contestId,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 255, message = "이름은 255자 이하로 입력해주세요.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
        String email,

        @NotNull(message = "역할은 필수입니다.")
        MemberRoleType role
) {
}
