package com.opus.opus.global.security.oauth2;

import com.opus.opus.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
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
        return CookieUtil.getCookie(request, "redirect_type")
                .map(Cookie::getValue)
                .map(type -> "local".equals(type) ? localRedirectCallbackUrl : prodRedirectCallbackUrl)
                .orElse(prodRedirectCallbackUrl);
    }
}
