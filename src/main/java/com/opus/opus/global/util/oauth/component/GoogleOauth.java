package com.opus.opus.global.util.oauth.component;


import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.FAILED_TO_GET_ACCESS_TOKEN;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.FAILED_TO_GET_SOCIAL_USER_INFO;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.SOCIAL_LOGIN_FAILED_AUTH_CODE;
import static com.opus.opus.global.util.oauth.exception.OAuthExceptionType.SOCIAL_LOGIN_SERVER_ERROR;
import static org.hibernate.internal.util.JdbcExceptionHelper.extractErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opus.opus.global.util.AuthRedisUtil;
import com.opus.opus.global.util.oauth.dto.GoogleOAuthToken;
import com.opus.opus.global.util.oauth.exception.OAuthException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {

    @Value("${spring.oauth2.google.url}")
    private String GOOGLE_SNS_URL;

    @Value("${spring.oauth2.google.client-id}")
    private String GOOGLE_SNS_CLIENT_ID;

    @Value("${spring.oauth2.google.callback-login-url}")
    private String GOOGLE_SNS_CALLBACK_LOGIN_URL;

    @Value("${spring.oauth2.google.frontend-local-callback-login-url}")
    private String GOOGLE_SNS_FRONTEND_LOCAL_CALLBACK_LOGIN_URL;

    @Value("${spring.oauth2.google.client-secret}")
    private String GOOGLE_SNS_CLIENT_SECRET;

    @Value("${spring.oauth2.google.scope}")
    private String GOOGLE_DATA_ACCESS_SCOPE;

    private static final long OAUTH_STATE_TTL = 5L;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private final AuthRedisUtil authRedisUtil;

    @Override
    public String getOauthRedirectURL() {
        String callbackUrl = determineCallbackUrl();

        HttpServletRequest request = getCurrentHttpRequest();
        String sessionId = request.getSession().getId();
        String state = UUID.randomUUID().toString();
        String stateKey = createOAuthStateKey(sessionId, state);
        authRedisUtil.set(stateKey, "valid", OAUTH_STATE_TTL, TimeUnit.MINUTES);

        return UriComponentsBuilder.fromUriString(GOOGLE_SNS_URL)
                .queryParam("scope", GOOGLE_DATA_ACCESS_SCOPE)
                .queryParam("response_type", "code")
                .queryParam("client_id", GOOGLE_SNS_CLIENT_ID)
                .queryParam("redirect_uri", callbackUrl)
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public <T> T getUserInfoByCode(String code, Class<T> userType) throws JsonProcessingException {
        ResponseEntity<String> requestAccessToken = requestAccessToken(code);
        GoogleOAuthToken oAuthToken = getAccessToken(requestAccessToken);
        ResponseEntity<String> userInfo = requestUserInfo(oAuthToken);
        return getUserInfo(userInfo, userType);
    }

    public String createOAuthStateKey(String sessionId, String state) {
        return "oauth:state:" + sessionId + ":" + state;
    }

    private String determineCallbackUrl() {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            String origin = request.getHeader("Origin");
            log.debug("감지된 Origin 헤더: {}", origin);

            if (origin != null && origin.contains("localhost:5173")) {
                return GOOGLE_SNS_FRONTEND_LOCAL_CALLBACK_LOGIN_URL;
            }

            return GOOGLE_SNS_CALLBACK_LOGIN_URL;

        } catch (Exception e) {
            log.error("콜백 URL 결정 중 오류 발생", e);
            return GOOGLE_SNS_CALLBACK_LOGIN_URL;
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("OAuth 인증 요청이 HTTP 요청 컨텍스트 외부에서 호출됨");
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }
        return attributes.getRequest();
    }

    private ResponseEntity<String> requestAccessToken(String code) {
        String GOOGLE_TOKEN_REQUEST_URL = "https://oauth2.googleapis.com/token";

        String callbackUrl = determineCallbackUrl();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", GOOGLE_SNS_CLIENT_ID);
        params.add("client_secret", GOOGLE_SNS_CLIENT_SECRET);
        params.add("redirect_uri", callbackUrl);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.postForEntity(
                            GOOGLE_TOKEN_REQUEST_URL, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity;
            } else {
                log.error("Google Access Token Request Failed with status: {}", responseEntity.getStatusCode());
                throw new OAuthException(SOCIAL_LOGIN_FAILED_AUTH_CODE);
            }
        } catch (RestClientException e) {
            log.error("Google Access Token Request Server Error: {}", e.getMessage());
            throw new OAuthException(SOCIAL_LOGIN_SERVER_ERROR);
        }
    }

    private GoogleOAuthToken getAccessToken(ResponseEntity<String> response) {
        try {
            GoogleOAuthToken oAuthToken =
                    objectMapper.readValue(response.getBody(), GoogleOAuthToken.class);
            if (oAuthToken == null || oAuthToken.accessToken() == null) {
                throw new OAuthException(FAILED_TO_GET_ACCESS_TOKEN);
            }
            return oAuthToken;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Google OAuth Token: {}", e.getMessage());
            throw new OAuthException(FAILED_TO_GET_ACCESS_TOKEN);
        }
    }

    private ResponseEntity<String> requestUserInfo(GoogleOAuthToken oAuthToken) {
        String GOOGLE_USERINFO_REQUEST_URL = "https://www.googleapis.com/oauth2/v1/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.accessToken());
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(
                    GOOGLE_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
        } catch (RestClientException e) {
            log.error("Google User Info Request Server Error: {}", e.getMessage());
            throw new OAuthException(FAILED_TO_GET_SOCIAL_USER_INFO);
        }
    }

    private <T> T getUserInfo(ResponseEntity<String> userInfoRes, Class<T> userType) {
        try {
            T googleUser = objectMapper.readValue(userInfoRes.getBody(), userType);
            if (googleUser == null) {
                throw new OAuthException(FAILED_TO_GET_SOCIAL_USER_INFO);
            }
            return googleUser;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Google User Info: {}", e.getMessage());
            throw new OAuthException(FAILED_TO_GET_SOCIAL_USER_INFO);
        }
    }
}
