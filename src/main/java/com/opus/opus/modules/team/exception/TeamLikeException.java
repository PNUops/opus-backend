package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class TeamLikeException extends BaseException {

    private final TeamLikeExceptionType exceptionType;

    public TeamLikeException(final TeamLikeExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
