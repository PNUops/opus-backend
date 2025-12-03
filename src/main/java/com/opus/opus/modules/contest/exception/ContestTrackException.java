package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class ContestTrackException extends BaseException {
    private final ContestTrackExceptionType exceptionType;

    public ContestTrackException(final ContestTrackExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public ContestTrackException(final ContestTrackExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
