package com.opus.opus.global.security.oauth2.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum OAuthExceptionType implements BaseExceptionType {

	SOCIAL_LOGIN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 서버와의 통신에 실패했습니다."),
	FAILED_TO_GET_SOCIAL_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 사용자 정보를 가져오는데 실패했습니다."),
	INVALID_OAUTH_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 OAuth 토큰 타입입니다."),
	UNSUPPORTED_SOCIAL_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 타입입니다."),
	;

	private final HttpStatus httpStatus;
	private final String errorMessage;

	OAuthExceptionType(final HttpStatus httpStatus, final String errorMessage) {
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
