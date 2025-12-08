package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class TeamAwardException extends BaseException {
    private final TeamAwardExceptionType exceptionType;

    public TeamAwardException(final TeamAwardExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public TeamAwardException(final TeamAwardExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
