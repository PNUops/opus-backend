package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestMemberExceptionType implements BaseExceptionType {

    INVALID_MEMBER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 회원 유형입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestMemberExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
