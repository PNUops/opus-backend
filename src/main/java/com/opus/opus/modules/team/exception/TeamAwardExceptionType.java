package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamAwardExceptionType implements BaseExceptionType {
    DUPLICATE_AWARD_IDS(HttpStatus.BAD_REQUEST, "중복된 ID가 포함되어 있습니다."),
    AWARD_NOT_IN_TEAM_CONTEST(HttpStatus.BAD_REQUEST, "해당 팀의 대회에 속하지 않은 수상이 포함되어 있습니다. ");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    TeamAwardExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
