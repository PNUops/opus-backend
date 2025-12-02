package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestExceptionType implements BaseExceptionType {

    CATEGORY_HAS_CONTEST(HttpStatus.CONFLICT, "해당 카테고리에 속한 대회가 존재합니다."),
    CONTEST_NAME_ALREADY_EXIST(HttpStatus.CONFLICT, "동일한 대회명이 있습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
