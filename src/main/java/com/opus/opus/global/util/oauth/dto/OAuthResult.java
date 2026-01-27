package com.opus.opus.global.util.oauth.dto;

public record OAuthResult<T>(
        T userInfo,
        String accessToken,
        String refreshToken
) {
}
