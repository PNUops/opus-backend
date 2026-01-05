package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestTrackExceptionType implements BaseExceptionType {

    TRACKNAME_DUPLICATED(HttpStatus.CONFLICT, "해당 대회에 동일한 트랙명이 있습니다."),
    NOT_FOUND_TRACK(HttpStatus.NOT_FOUND, "존재하지 않는 분과입니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestTrackExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
