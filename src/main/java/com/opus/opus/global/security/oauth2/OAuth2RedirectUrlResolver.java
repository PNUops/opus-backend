package com.opus.opus.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2RedirectUrlResolver {

    @Value("${oauth2.redirect-callback-url.local}")
    private String localRedirectCallbackUrl;

    @Value("${oauth2.redirect-callback-url.prod}")
    private String prodRedirectCallbackUrl;

    public String resolve(final HttpServletRequest request) {
        final String origin = request.getHeader("Origin");
        log.debug("감지된 Origin 헤더: {}", origin);

        if (origin != null && origin.contains("localhost:5173")) {
            return localRedirectCallbackUrl;
        }
        return prodRedirectCallbackUrl;
    }
}
