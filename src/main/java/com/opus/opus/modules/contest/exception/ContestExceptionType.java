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
    CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD(HttpStatus.BAD_REQUEST, "투표 진행중에는 최대 투표 개수를 변경할 수 없습니다."),
    NOT_FOUND_CONTEST_SORT(HttpStatus.NOT_FOUND, "존재하는 팀 정렬이 없습니다"),
    ONLY_CUSTOM_MODE_CAN_CHANGE(HttpStatus.FORBIDDEN, "CUSTOM 모드에서만 정렬을 수정할 수 있습니다."),
    DUPLICATE_TEAM_ID_IN_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 팀ID가 있습니다."),
    DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST(HttpStatus.BAD_REQUEST, "중복된 itemOrder가 있습니다."),
    NOT_EXIST_TEAM_IN_CONTEST(HttpStatus.NOT_FOUND, "현재 대회에 소속된 팀이 아닙니다"),
    INVALID_CONTEST_SORT_CUSTOM_REQUEST(HttpStatus.BAD_REQUEST, "저장된 팀 개수와 request의 팀 개수가 다릅니다"),
    NOT_ALLOWED_DURING_VOTING_PERIOD(HttpStatus.BAD_REQUEST, "현재 투표 기간이므로 해당 작업을 수행할 수 없습니다."),
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
