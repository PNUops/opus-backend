package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestMemberExceptionType implements BaseExceptionType {

    INVALID_MEMBER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 회원 유형입니다."),
    DUPLICATE_MEMBER(HttpStatus.BAD_REQUEST, "중복된 회원이 포함되어 있습니다."),
    INVALID_TEAM_FOR_CONTEST(HttpStatus.BAD_REQUEST, "해당 대회에 속하지 않는 팀입니다."),
    ALREADY_ASSIGNED_MEMBER(HttpStatus.CONFLICT, "이미 배정된 회원입니다."),
    NOT_FOUND_CONTEST_MEMBER(HttpStatus.NOT_FOUND, "배정 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestMemberExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
