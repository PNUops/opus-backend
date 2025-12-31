package com.opus.opus.modules.team.exception;


import com.opus.opus.global.base.BaseException;
import com.opus.opus.global.base.BaseExceptionType;

public class TeamCommentException extends BaseException {

	private final TeamCommentExceptionType exceptionType;

	public TeamCommentException(final TeamCommentExceptionType exceptionType) {
		super(exceptionType.errorMessage());
		this.exceptionType = exceptionType;
	}

	public TeamCommentException(final TeamCommentExceptionType exceptionType, final String message) {
		super(message);
		this.exceptionType = exceptionType;
	}

	@Override
	public BaseExceptionType exceptionType() {
		return exceptionType;
	}
}

