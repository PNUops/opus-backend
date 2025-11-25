package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamExceptionType implements BaseExceptionType {
    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    NOT_TEAM_LEADER(HttpStatus.FORBIDDEN, "해당 팀의 팀장 권한이 없습니다."),
    EXIST_ONLY_ONE_ABOUT_TEAM_SORT(HttpStatus.NOT_FOUND, "팀 정렬 테이블의 id는 1번만 존재합니다."),
    ONLY_CUSTOM_MODE_CAN_CHANGE(HttpStatus.FORBIDDEN, "CUSTOM 모드에서만 정렬을 수정할 수 있습니다."),
    MUST_FILL_FIELD(HttpStatus.BAD_REQUEST, "필드를 작성해야 합니다."),

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
