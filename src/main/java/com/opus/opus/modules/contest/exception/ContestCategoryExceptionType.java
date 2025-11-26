package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestCategoryExceptionType implements BaseExceptionType {

    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    CATEGORY_NAME_ALREADY_EXIST(HttpStatus.CONFLICT, "동일한 카테고리명이 있습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestCategoryExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
