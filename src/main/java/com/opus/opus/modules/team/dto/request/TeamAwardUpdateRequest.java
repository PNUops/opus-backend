package com.opus.opus.modules.team.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TeamAwardUpdateRequest(
        @NotNull(message = "수상 ID 목록은 필수입니다.")
        List<Long> awardIds
) {
}
