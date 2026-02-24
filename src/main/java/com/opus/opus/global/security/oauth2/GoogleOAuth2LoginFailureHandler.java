package com.opus.opus.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleOAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${oauth2.redirect.url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error("OAuth2 로그인 실패: {}", exception.getMessage());

        final String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        final String targetUrl = redirectUrl + "?error=" + errorMessage;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
