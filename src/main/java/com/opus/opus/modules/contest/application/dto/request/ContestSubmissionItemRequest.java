package com.opus.opus.modules.contest.application.dto.request;

import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionItemRequest(

        @NotBlank(message = "제출물 종류 이름은 비어 있을 수 없습니다.")
        String name,

        Long contestTrackId,

        String description,

        @NotEmpty(message = "허용 파일 형식은 최소 1개 이상이어야 합니다.")
        List<SubmissionFileFormat> allowedFileFormats,

        @NotNull(message = "파일 크기 제한은 필수입니다.")
        @Positive(message = "파일 크기 제한은 0보다 커야 합니다.")
        Integer maxFileSizeMb,

        @NotNull(message = "파일 수 제한은 필수입니다.")
        @Positive(message = "파일 수 제한은 0보다 커야 합니다.")
        Integer maxFileCount,

        @NotNull(message = "시작일시는 필수입니다.")
        LocalDateTime startAt,

        @NotNull(message = "마감일시는 필수입니다.")
        LocalDateTime endAt,

        @NotNull(message = "지각 제출 허용 여부는 필수입니다.")
        Boolean allowLateSubmission,

        @NotNull(message = "공개 범위는 필수입니다.")
        SubmissionVisibility visibility
) {
}
