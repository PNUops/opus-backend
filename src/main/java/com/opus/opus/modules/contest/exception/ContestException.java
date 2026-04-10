package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse.TeamBulkError;
import java.util.List;
import lombok.Getter;

@Getter
public class ContestException extends BaseException {
    private final ContestExceptionType exceptionType;
    private final List<TeamBulkError> bulkErrors;

    public ContestException(final ContestExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
        this.bulkErrors = null;
    }

    public ContestException(final ContestExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
        this.bulkErrors = null;
    }

    public ContestException(final ContestExceptionType exceptionType, final List<TeamBulkError> bulkErrors) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
        this.bulkErrors = bulkErrors;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
