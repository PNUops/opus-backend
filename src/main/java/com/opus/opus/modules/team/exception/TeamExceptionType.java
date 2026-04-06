package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamExceptionType implements BaseExceptionType {

    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "팀이 존재하지 않습니다."),
    CONTEST_HAS_TEAM(HttpStatus.CONFLICT, "해당 대회에 속한 팀이 존재합니다."),
    TRACK_HAS_TEAM(HttpStatus.CONFLICT, "해당 분과에 속한 팀이 존재합니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "팀 상세보기의 필수 항목이 누락되었습니다."),
    FORBIDDEN_CONTEST_OR_TRACK_UPDATE(HttpStatus.FORBIDDEN, "대회 또는 분과는 변경할 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    TeamExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
