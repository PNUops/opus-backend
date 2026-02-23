package com.opus.opus.modules.team.application.dto.request;

import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TeamMemberCreateRequest(

        @NotBlank(message = "추가할 팀원명은 비어 있을 수 없습니다.")
        String memberName,

        @NotBlank(message = "추가할 팀원학번은 비어 있을 수 없습니다.")
        @Pattern(regexp = "^\\d{9}$", message = "학번은 9자리 숫자여야 합니다.")
        String memberStudentId,

        @NotNull(message = "권한을 입력해 주세요.")
        TeamMemberRoleType roleType
) {
}
