package com.opus.opus.modules.team.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TeamContestAwardUpdateRequest(
        @NotBlank(message = "수상 ID 목록은 필수입니다.")
        List<Long> awardIds
) {
}
