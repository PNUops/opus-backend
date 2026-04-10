package com.opus.opus.modules.contest.application.dto.response;

import java.util.List;

public record TeamBulkErrorResponse(
        List<TeamBulkError> errors
) {
    public record TeamBulkError(
            int rowNumber,
            String message
    ) {
    }
}
