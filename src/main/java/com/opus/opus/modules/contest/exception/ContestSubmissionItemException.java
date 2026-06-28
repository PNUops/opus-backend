package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestSubmissionItemException extends BaseException {
    private final ContestSubmissionItemExceptionType exceptionType;

    public ContestSubmissionItemException(final ContestSubmissionItemExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestSubmissionItemException(final ContestSubmissionItemExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
