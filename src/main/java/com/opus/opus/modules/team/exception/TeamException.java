package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse.TeamBulkError;
import java.util.List;
import lombok.Getter;

@Getter
public class TeamException extends BaseException {

    private final TeamExceptionType exceptionType;
    private final List<TeamBulkError> bulkErrors;

    public TeamException(final TeamExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
        this.bulkErrors = null;
    }

    public TeamException(final TeamExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.bulkErrors = null;
    }

    public TeamException(final TeamExceptionType exceptionType, final List<TeamBulkError> bulkErrors) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
        this.bulkErrors = bulkErrors;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
