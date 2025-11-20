package com.opus.opus.modules.member.exception;

import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class MemberException extends BaseException {

    private final MemberExceptionType exceptionType;

    public MemberException(final MemberExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }

    public MemberException(final MemberExceptionType exceptionType, final String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
