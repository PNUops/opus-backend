package com.opus.opus.modules.contest.application.dto.response;

import java.util.List;

public record TeamBulkUploadResponse(
        int teamCount,
        List<TeamBulkResult> teams
) {
    public record TeamBulkResult(
            int rowNumber,
            String teamName,
            Long teamId
    ) {
    }
}
