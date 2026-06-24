package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ArchiveRequest(

        @NotEmpty(message = "다운로드 대상은 비어 있을 수 없습니다.")
        @Valid
        List<ArchiveTargetRequest> targets
) {
}
