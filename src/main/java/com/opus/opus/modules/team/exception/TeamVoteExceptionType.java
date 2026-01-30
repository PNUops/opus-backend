package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamVoteExceptionType implements BaseExceptionType {

    ALREADY_VOTED(HttpStatus.BAD_REQUEST, "이미 투표한 팀입니다."),
    ALREADY_UNVOTED(HttpStatus.BAD_REQUEST, "이미 투표를 취소한 팀입니다."),
    VOTE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "대회당 최대 %d개 팀만 투표할 수 있습니다."),
    DUPLICATE_VOTE_REQUEST(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    NOT_VOTED_YET(HttpStatus.BAD_REQUEST, "아직 투표하지 않은 팀입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TeamVoteExceptionType(final HttpStatus httpStatus, final String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }

    @Override
    public String errorMessage() {
        return message;
    }
}
