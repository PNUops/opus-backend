package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestExceptionType implements BaseExceptionType {
    NOT_FOUND_CONTEST(HttpStatus.NOT_FOUND, "존재하지 않는 대회입니다."),
    CONTEST_NAME_ALREADY_EXIST(HttpStatus.CONFLICT, "동일한 대회명이 있습니다."),
    CONTEST_HAS_TEAMS(HttpStatus.CONFLICT, "먼저 해당 대회의 모든 팀을 삭제해주세요."),
    NOT_FOUND_CURRENT_CONTEST(HttpStatus.NOT_FOUND, "현재 진행 중인 대회가 없습니다."),
    CANNOT_CHANGE_CONTEST_FOR_CURRENT(HttpStatus.FORBIDDEN, "현재 진행 중인 대회의 팀의 대회를 수정할 수 없습니다."),
    CANNOT_UPDATE_TEAM_INFO_FOR_CURRENT(HttpStatus.FORBIDDEN, "현재 진행 중인 대회의 팀의 팀명, 프로젝트명, 팀장, 팀원 정보를 수정할 수 없습니다."),
    ADMIN_ONLY_FOR_PAST_CONTEST(HttpStatus.FORBIDDEN, "관리자만 과거 대회의 정보를 수정할 수 있습니다."),
    CANNOT_CREATE_TEAM_OF_CURRENT_CONTEST(HttpStatus.FORBIDDEN, "현재 진행 중인 대회의 새로운 대회를 생성할 수 없습니다."),
    VOTE_END_PRECEDE_VOTE_START(HttpStatus.BAD_REQUEST, "투표 종료가 투표 시작보다 빠를 수 없습니다."),
    NOT_VOTE_PERIOD_NOW(HttpStatus.BAD_REQUEST, "지금은 투표 기간이 아닙니다."),
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
