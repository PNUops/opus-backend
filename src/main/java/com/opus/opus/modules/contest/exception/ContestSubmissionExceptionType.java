package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionExceptionType implements BaseExceptionType {

    NOT_FOUND_SUBMISSION(HttpStatus.NOT_FOUND, "존재하지 않는 제출물입니다."),
    NO_SUBMISSIONS_TO_DOWNLOAD(HttpStatus.NOT_FOUND, "다운로드할 제출물이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestSubmissionExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
