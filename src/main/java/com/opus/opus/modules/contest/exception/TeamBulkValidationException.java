package com.opus.opus.modules.contest.exception;

import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse;
import lombok.Getter;

@Getter
public class TeamBulkValidationException extends RuntimeException {

    private final TeamBulkErrorResponse errorResponse;

    public TeamBulkValidationException(final TeamBulkErrorResponse errorResponse) {
        super("팀 일괄 등록 유효성 검사에 실패했습니다.");
        this.errorResponse = errorResponse;
    }
}
