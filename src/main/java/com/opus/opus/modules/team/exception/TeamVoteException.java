package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class TeamVoteException extends BaseException {

    private final TeamVoteExceptionType exceptionType;

    public TeamVoteException(final TeamVoteExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public TeamVoteException(final TeamVoteExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
