package com.opus.opus.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
        final HttpSession session = request.getSession(false);
        final String redirect = session != null ? (String) session.getAttribute("redirect") : null;

        log.info("세션에서 감지된 redirect: {}", redirect);

        if ("local".equals(redirect)) {
            return localRedirectCallbackUrl;
        }
        return prodRedirectCallbackUrl;
    }
}
