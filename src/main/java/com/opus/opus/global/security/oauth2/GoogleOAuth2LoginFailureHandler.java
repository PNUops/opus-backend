package com.opus.opus.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2RedirectUrlResolver redirectUrlResolver;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 로그인 실패 원인 : {}", exception.getMessage());

        final String encodedErrorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        final String targetUrl = redirectUrlResolver.resolve(request) + "?error=" + encodedErrorMessage;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
