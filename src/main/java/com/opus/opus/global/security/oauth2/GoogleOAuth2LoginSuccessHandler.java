package com.opus.opus.global.security.oauth2;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final OAuth2RedirectUrlResolver redirectUrlResolver;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final GoogleOAuth2MemberDetails oAuth2MemberDetails = (GoogleOAuth2MemberDetails) authentication.getPrincipal();
        final Member member = oAuth2MemberDetails.getMember();

        final List<String> roles = member.getRoles().stream()
                .map(MemberRoleType::toString)
                .toList();

        final String token = jwtProvider.createToken(String.valueOf(member.getId()), roles, member.getName());

        final String targetUrl = UriComponentsBuilder.fromUriString(redirectUrlResolver.resolve(request))
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
