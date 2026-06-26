package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ContestSubmissionFeedbackSaveRequest(

        @NotBlank(message = "피드백 본문은 필수입니다.")
        String description,

        List<Long> removeFileIds
) {
}
