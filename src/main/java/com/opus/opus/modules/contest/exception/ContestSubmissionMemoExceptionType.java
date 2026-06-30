package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionMemoExceptionType implements BaseExceptionType {

    NOT_FOUND_MEMO(HttpStatus.NOT_FOUND, "존재하지 않는 메모입니다."),
    MEMO_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 메모가 존재합니다."),
    INVALID_SUBMISSION_FOR_TEAM(HttpStatus.FORBIDDEN, "해당 팀의 제출물이 아닙니다."),
    INVALID_SUBMISSION_FOR_CONTEST(HttpStatus.FORBIDDEN, "해당 대회의 제출물이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestSubmissionMemoExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
