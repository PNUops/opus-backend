package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamLikeExceptionType implements BaseExceptionType {

    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요한 팀입니다."),
    ALREADY_UNLIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 취소한 팀입니다."),
    VOTE_PERIOD_NOW(HttpStatus.BAD_REQUEST, "투표 기간에는 좋아요를 할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    TeamLikeExceptionType(final HttpStatus httpStatus, final String message) {
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
