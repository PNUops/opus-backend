package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestTemplateException extends BaseException {
    private final ContestTemplateExceptionType exceptionType;

    public ContestTemplateException(final ContestTemplateExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestTemplateException(final ContestTemplateExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
