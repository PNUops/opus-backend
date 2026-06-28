package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StaffBatchAssignRequest(

        @NotEmpty(message = "배정할 회원을 선택해 주세요.")
        List<Long> memberIds,

        @NotEmpty(message = "담당 팀을 선택해 주세요.")
        List<Long> teamIds
) {
}
