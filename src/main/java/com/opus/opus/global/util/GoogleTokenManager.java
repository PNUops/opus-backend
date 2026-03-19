package com.opus.opus.global.util;

import com.opus.opus.global.security.oauth2.GoogleToken;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleTokenManager {

    private final AuthRedisUtil authRedisUtil;
    private final RestTemplate restTemplate;
    private final TextEncryptor textEncryptor;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private static final String GOOGLE_TOKEN_KEY_PREFIX = "oauth2:google:token:";
    private static final long GOOGLE_TOKEN_TTL = 3L;

    public void save(final Long memberId, final String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return;
        }
        authRedisUtil.set(generateKey(memberId), textEncryptor.encrypt(refreshToken), GOOGLE_TOKEN_TTL, TimeUnit.HOURS);
    }

    public Optional<GoogleToken> get(final Long memberId) {
        return Optional.ofNullable(authRedisUtil.get(generateKey(memberId)))
                .map(encrypted -> new GoogleToken(textEncryptor.decrypt(encrypted)));
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

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(params, headers),
                Map.class
        );
        return (String) response.getBody().get("access_token");
    }

    public void revoke(final String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);

        restTemplate.postForEntity(
                "https://oauth2.googleapis.com/revoke",
                new HttpEntity<>(params, headers),
                String.class
        );
    }
}
