package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionItemExceptionType implements BaseExceptionType {

    NOT_FOUND_SUBMISSION_ITEM(HttpStatus.NOT_FOUND, "존재하지 않는 제출 항목입니다."),
    INVALID_SUBMISSION_ITEM_FOR_CONTEST(HttpStatus.BAD_REQUEST, "해당 대회에 속하지 않는 제출 항목입니다."),
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
