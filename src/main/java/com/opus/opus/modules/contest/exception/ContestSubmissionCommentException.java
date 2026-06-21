package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestSubmissionCommentException extends BaseException {

    private final ContestSubmissionCommentExceptionType exceptionType;

    public ContestSubmissionCommentException(final ContestSubmissionCommentExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
