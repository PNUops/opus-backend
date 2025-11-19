package com.opus.opus.modules.file.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum FileExceptionType implements BaseExceptionType {

    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    FileExceptionType(final HttpStatus httpStatus, final String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }
}
