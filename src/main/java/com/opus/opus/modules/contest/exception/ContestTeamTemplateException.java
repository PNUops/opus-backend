package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestTeamTemplateException extends BaseException {
    private final ContestTeamTemplateExceptionType exceptionType;

    public ContestTeamTemplateException(final ContestTeamTemplateExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestTeamTemplateException(final ContestTeamTemplateExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}

