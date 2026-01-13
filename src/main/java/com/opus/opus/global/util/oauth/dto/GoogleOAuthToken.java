package com.opus.opus.global.util.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleOAuthToken(
	@JsonProperty("access_token")
	String accessToken
) {
}
