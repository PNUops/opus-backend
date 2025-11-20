package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class TeamException extends BaseException {

    private final TeamExceptionType exceptionType;

    public TeamException(final TeamExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public TeamException(final TeamExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
