package com.opus.opus.modules.team.application.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record TeamExcelRow(
        Integer rowNumber,
        String teamName,
        String projectName,
        String leaderName,
        String leaderStudentId,
        List<String> memberNames,
        List<String> memberStudentIds
) {
}
