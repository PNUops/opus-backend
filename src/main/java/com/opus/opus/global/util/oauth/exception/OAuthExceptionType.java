package com.opus.opus.global.util.oauth.exception;

import com.opus.opus.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum OAuthExceptionType implements BaseExceptionType {

	SOCIAL_LOGIN_FAILED_AUTH_CODE(HttpStatus.BAD_REQUEST, "소셜 로그인 인증 코드가 유효하지 않습니다."),
	SOCIAL_LOGIN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 서버와의 통신에 실패했습니다."),
	FAILED_TO_GET_ACCESS_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "액세스 토큰을 가져오는데 실패했습니다."),
	FAILED_TO_GET_SOCIAL_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 사용자 정보를 가져오는데 실패했습니다."),
	INVALID_OAUTH_TOKEN_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 OAuth 토큰 타입입니다."),
	OAUTH_AUTHORIZATION_FAILED(HttpStatus.UNAUTHORIZED, "소셜 로그인 인증에 실패했습니다."),
    USER_DENIED_AUTHORIZATION(HttpStatus.BAD_REQUEST, "사용자가 권한 요청을 거부했습니다."),
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
