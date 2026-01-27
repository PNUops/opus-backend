package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestExceptionType implements BaseExceptionType {
    NOT_FOUND_CONTEST(HttpStatus.NOT_FOUND, "존재하지 않는 대회입니다."),
    CONTEST_HAS_TEAMS(HttpStatus.CONFLICT, "먼저 해당 대회의 모든 팀을 삭제해주세요."),
    CURRENT_CONTEST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "현재 진행 중인 대회는 최대 2개까지 설정할 수 있습니다."),
    ALREADY_CURRENT_CONTEST(HttpStatus.BAD_REQUEST, "이미 현재 대회입니다."),
    ALREADY_NOT_CURRENT_CONTEST(HttpStatus.BAD_REQUEST, "이미 현재 대회가 아닙니다."),
    CATEGORY_HAS_CONTEST(HttpStatus.CONFLICT, "해당 카테고리에 속한 대회가 존재합니다."),
    CONTEST_NAME_ALREADY_EXIST(HttpStatus.CONFLICT, "동일한 대회명이 있습니다."),
    VOTE_END_PRECEDE_VOTE_START(HttpStatus.BAD_REQUEST, "투표 종료가 투표 시작보다 빠를 수 없습니다."),
    NOT_ALLOWED_DURING_VOTING_PERIOD(HttpStatus.BAD_REQUEST, "현재 투표 기간이므로 해당 작업을 수행할 수 없습니다.")
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
