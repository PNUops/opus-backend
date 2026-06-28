package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionFeedbackExceptionType implements BaseExceptionType {

    NOT_FOUND_FEEDBACK(HttpStatus.NOT_FOUND, "존재하지 않는 피드백입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestSubmissionFeedbackExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
