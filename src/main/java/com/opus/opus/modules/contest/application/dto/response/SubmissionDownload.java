package com.opus.opus.modules.contest.application.dto.response;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record SubmissionDownload(

        String fileName,
        StreamingResponseBody body
) {
}
