package com.opus.opus.modules.team.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum TeamCommentExceptionType implements BaseExceptionType {
	NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
	NOT_OWNER_COMMENT(HttpStatus.FORBIDDEN, "댓글 작성자가 아닙니다.");

	private final HttpStatus httpStatus;
	private final String errorMessage;

	TeamCommentExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
