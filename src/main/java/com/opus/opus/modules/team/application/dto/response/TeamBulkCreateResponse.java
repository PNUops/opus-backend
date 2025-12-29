package com.opus.opus.modules.team.application.dto.response;

import com.opus.opus.modules.team.application.dto.TeamBulkResult;
import java.util.List;

public record TeamBulkCreateResponse(
        Integer successCount,
        List<TeamBulkResult> results
) {
    public static TeamBulkCreateResponse of(List<TeamBulkResult> results) {
        return new TeamBulkCreateResponse(results.size(), results);
    }
}
