package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;
import lombok.Getter;

@Getter
public class ContestSubmissionException extends BaseException {

    private final ContestSubmissionExceptionType exceptionType;

    public ContestSubmissionException(final ContestSubmissionExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestSubmissionException(final ContestSubmissionExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
