package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestAwardExceptionType implements BaseExceptionType {

    DUPLICATE_CONTEST_AWARD_NAME(HttpStatus.BAD_REQUEST, "해당 대회에 이미 동일한 수상명이 존재합니다."),
    NOT_FOUND_CONTEST_AWARD(HttpStatus.NOT_FOUND, "존재하지 않는 수상입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestAwardExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
