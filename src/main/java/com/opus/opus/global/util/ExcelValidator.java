package com.opus.opus.global.util;

import com.opus.opus.modules.team.application.dto.TeamExcelRow;
import com.opus.opus.modules.team.exception.TeamException;
import org.springframework.stereotype.Component;

import static com.opus.opus.modules.team.exception.TeamExceptionType.*;

@Component
public class ExcelValidator {

    public void validate(final TeamExcelRow row) {
        validateTeamName(row);
        validateProjectName(row);
        validateLeaderInfo(row);
        validateMemberInfo(row);
    }

    private void validateTeamName(final TeamExcelRow row) {
        if (isNullOrEmpty(row.teamName())) {
            throw new TeamException(TEAM_NAME_REQUIRED, row.rowNumber());
        }
    }

    private void validateProjectName(final TeamExcelRow row) {
        if (isNullOrEmpty(row.projectName())) {
            throw new TeamException(PROJECT_NAME_REQUIRED, row.rowNumber());
        }
    }

    private void validateLeaderInfo(final TeamExcelRow row) {
        if (isNullOrEmpty(row.leaderName())) {
            throw new TeamException(LEADER_NAME_REQUIRED, row.rowNumber());
        }
        if (isNullOrEmpty(row.leaderStudentId())) {
            throw new TeamException(LEADER_STUDENT_ID_REQUIRED, row.rowNumber());
        }
    }

    private void validateMemberInfo(final TeamExcelRow row) {
        int memberNameCount = row.memberNames() != null ? row.memberNames().size() : 0;
        int memberStudentIdCount = row.memberStudentIds() != null ? row.memberStudentIds().size() : 0;

        if (memberNameCount != memberStudentIdCount) {
            throw new TeamException(MEMBER_COUNT_MISMATCH, row.rowNumber());
        }
    }

    private boolean isNullOrEmpty(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
