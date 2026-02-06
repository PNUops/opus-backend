package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamExceptionType implements BaseExceptionType {

    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "팀이 존재하지 않습니다."),
    CONTEST_HAS_TEAM(HttpStatus.CONFLICT, "해당 대회에 속한 팀이 존재합니다."),
    TRACK_HAS_TEAM(HttpStatus.CONFLICT, "해당 분과에 속한 팀이 존재합니다."),
    INVALID_ITEM_ORDER(HttpStatus.BAD_REQUEST, "적절하지 않은 itemOrder입니다.(최대 팀 개수보다 초과된 itemOrder)"),
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
