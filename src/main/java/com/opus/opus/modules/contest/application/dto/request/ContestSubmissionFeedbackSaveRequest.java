package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ContestSubmissionFeedbackSaveRequest(

        @NotBlank(message = "피드백 본문은 필수입니다.")
        @Size(max = 3000, message = "피드백 본문은 최대 3000자까지 작성할 수 있습니다.")
        String description,
        List<Long> removeFileIds
) {
}
