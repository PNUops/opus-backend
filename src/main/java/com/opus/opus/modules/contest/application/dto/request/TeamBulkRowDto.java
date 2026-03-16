package com.opus.opus.modules.contest.application.dto.request;

import java.util.List;

public record TeamBulkRowDto(
        int rowNumber,
        String teamName,
        String projectName,
        String leaderName,
        String leaderStudentId,
        String leaderEmail,
        List<String> memberNames,
        List<String> memberStudentIds,
        List<String> memberEmails
) {
}
