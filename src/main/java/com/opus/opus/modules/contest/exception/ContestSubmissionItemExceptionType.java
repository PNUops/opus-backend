package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionItemExceptionType implements BaseExceptionType {

    INVALID_SUBMISSION_PERIOD(HttpStatus.BAD_REQUEST, "마감일시는 시작일시보다 이후여야 합니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestSubmissionItemExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
