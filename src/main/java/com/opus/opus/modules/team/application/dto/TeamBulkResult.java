package com.opus.opus.modules.team.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeamBulkResult(
        Integer rowNumber,
        String teamName,
        Long teamId
) {
    public static TeamBulkResult success(Integer rowNumber, String teamName, Long teamId) {
        return new TeamBulkResult(rowNumber, teamName, teamId);
    }
}
