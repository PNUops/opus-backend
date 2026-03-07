package com.opus.opus.global.security.oauth2;

import static com.opus.opus.modules.member.exception.MemberExceptionType.GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.SocialType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final OAuth2User oAuth2User = super.loadUser(userRequest);

        final Map<String, Object> attributes = oAuth2User.getAttributes();
        final String socialId = (String) attributes.get("sub");
        final String email = (String) attributes.get("email");
        final String name = (String) attributes.get("name");

        final Member member = memberRepository.findBySocialTypeAndSocialId(SocialType.GOOGLE, socialId)
                .orElseGet(() -> findOrRegisterSocialMember(name, email, socialId));

        return new GoogleOAuth2MemberDetails(member, attributes);
    }

    private Member findOrRegisterSocialMember(final String name, final String email, final String socialId) {
        return memberRepository.findByEmail(email)
                .map(this::validateSocialMember)
                .orElseGet(() -> registerNewSocialMember(name, email, socialId));
    }

    private Member validateSocialMember(final Member member) {
        if (!member.isSocialMember()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN.errorMessage()),
                    GENERAL_MEMBER_CANNOT_USE_SOCIAL_LOGIN.errorMessage()
            );
        }
        return member;
    }

    private Member registerNewSocialMember(final String name, final String email, final String socialId) {
        return memberRepository.save(Member.socialMember()
                .name(name)
                .email(email)
                .socialType(SocialType.GOOGLE)
                .socialId(socialId)
                .roles(Set.of(MemberRoleType.ROLE_회원))
                .build());
    }
}
