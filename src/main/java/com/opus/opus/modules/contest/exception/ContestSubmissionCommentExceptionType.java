package com.opus.opus.modules.contest.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ContestSubmissionCommentExceptionType implements BaseExceptionType {

    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "존재하지 않는 코멘트입니다."),
    NOT_OWNER_COMMENT(HttpStatus.FORBIDDEN, "코멘트 작성자가 아닙니다."),
    COMMENT_NOT_BELONG_TO_SUBMISSION(HttpStatus.BAD_REQUEST, "코멘트가 해당 제출물에 속해있지 않습니다."),
    NOTHING_TO_UPDATE(HttpStatus.BAD_REQUEST, "수정할 내용이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String errorMessage;

    ContestSubmissionCommentExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
