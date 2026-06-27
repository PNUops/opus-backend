package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record DownloadTargetRequest(

        @NotNull(message = "제출물 종류 ID는 필수입니다.")
        Long submissionTypeId,
        Long trackId
) {
}
