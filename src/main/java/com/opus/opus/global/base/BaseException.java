package com.opus.opus.global.base;

public abstract class BaseException extends RuntimeException {

    protected BaseException(final String message) {
        super(message);
    }

    protected BaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public abstract BaseExceptionType exceptionType();
}
