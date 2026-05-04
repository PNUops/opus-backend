package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamLikeExceptionType implements BaseExceptionType {

    DUPLICATE_LIKE_REQUEST(HttpStatus.CONFLICT, "이미 처리된 요청입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TeamLikeExceptionType(final HttpStatus httpStatus, final String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String errorMessage() {
        return message;
    }
}
