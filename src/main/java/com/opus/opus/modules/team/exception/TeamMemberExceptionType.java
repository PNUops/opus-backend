package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamMemberExceptionType implements BaseExceptionType {

    TEAM_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팀에 등록된 팀원입니다."),
    TEAM_MEMBER_NOT_FOUND_IN_TEAM(HttpStatus.NOT_FOUND, "해당 팀의 팀원이 아닙니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    TeamMemberExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
