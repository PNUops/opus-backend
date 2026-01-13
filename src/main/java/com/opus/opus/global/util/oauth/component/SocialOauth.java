package com.opus.opus.global.util.oauth.component;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface SocialOauth {

	/**
	 * 각 소셜 로그인 페이지로 리다이렉트할 URL을 빌드합니다.
	 * 사용자로부터 로그인 요청을 받아 소셜 로그인 서버 인증용 코드를 요청합니다.
	 */
	String getOauthRedirectURL();

	/**
	 * 인가 코드를 사용하여 사용자 정보를 가져옵니다.
	 * 내부적으로 액세스 토큰 요청 -> 사용자 정보 요청 과정을 처리합니다.
	 * @param code 인가 코드
	 * @param userType 반환할 사용자 객체 클래스 (예: GoogleUser.class)
	 * @return 파싱된 사용자 객체
	 * @throws JsonProcessingException JSON 파싱 실패 시 발생
	 */
	<T> T getUserInfoByCode(String code, Class<T> userType) throws JsonProcessingException;
}
