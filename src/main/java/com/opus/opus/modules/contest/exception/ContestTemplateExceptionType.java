package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestTemplateExceptionType implements BaseExceptionType {
    NOT_FOUND_TEMPLATE(HttpStatus.NOT_FOUND, "템플릿을 찾을 수 없습니다."),
    ;
    
    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestTemplateExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
