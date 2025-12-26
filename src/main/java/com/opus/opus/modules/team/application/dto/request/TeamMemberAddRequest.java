package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TeamMemberAddRequest(
        @NotBlank(message = "추가할 팀원명은 비어 있을 수 없습니다.")
        String teamMemberName,

        @NotBlank(message = "추가할 팀원학번은 비어 있을 수 없습니다.")
        String teamMemberStudentId
) {
}
