package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestCategoryException extends BaseException {
    private final ContestCategoryExceptionType exceptionType;

    public ContestCategoryException(final ContestCategoryExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestCategoryException(final ContestCategoryExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
