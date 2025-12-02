package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestExceptionType implements BaseExceptionType {
    NOT_FOUND_CONTEST(HttpStatus.NOT_FOUND, "존재하지 않는 대회입니다."),
    CONTEST_HAS_TEAMS(HttpStatus.CONFLICT, "먼저 해당 대회의 모든 팀을 삭제해주세요."),

    ;

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
