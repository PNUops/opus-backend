package com.opus.opus.global.security.oauth2;

import com.opus.opus.modules.member.domain.Member;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class GoogleOAuth2MemberDetails implements OAuth2User {

    @Getter
    private final Member member;
    private final Map<String, Object> attributes;

    public GoogleOAuth2MemberDetails(final Member member, final Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .toList();
    }

    @Override
    public String getName() {
        return member.getEmail();
    }
}
