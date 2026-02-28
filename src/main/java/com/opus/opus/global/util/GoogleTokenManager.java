package com.opus.opus.global.util;

import com.opus.opus.global.security.oauth2.GoogleToken;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleTokenManager {

    private final AuthRedisUtil authRedisUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    private static final String GOOGLE_TOKEN_KEY_PREFIX = "oauth2:google:token:";
    private static final long GOOGLE_TOKEN_TTL = 3L;
    private static final String DELIMITER = "|";

    public void save(final Long memberId, final String accessToken, final String refreshToken) {
        authRedisUtil.set(
                generateKey(memberId),
                accessToken + DELIMITER + refreshToken,
                GOOGLE_TOKEN_TTL,
                TimeUnit.HOURS
        );
    }

    public Optional<GoogleToken> get(final Long memberId) {
        return Optional.ofNullable(authRedisUtil.get(generateKey(memberId)))
                .map(this::parseGoogleToken);
    }

    private GoogleToken parseGoogleToken(final String value) {
        final String[] tokens = value.split(DELIMITER, 2);
        return new GoogleToken(tokens[0], tokens.length > 1 ? tokens[1] : "");
    }

    public void delete(final Long memberId) {
        authRedisUtil.delete(generateKey(memberId));
    }

    private String generateKey(final Long memberId) {
        return GOOGLE_TOKEN_KEY_PREFIX + memberId;
    }

    public String refreshAccessToken(final String refreshToken) {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        final ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(params),
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    public void revoke(final String accessToken) {
        restTemplate.postForEntity(
                "https://oauth2.googleapis.com/revoke?token=" + accessToken,
                null, String.class);
    }
}
