package com.opus.opus.global.security.oauth2;

import com.opus.opus.modules.member.domain.Member;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2MemberDetails extends OAuth2User {
    Member getMember();
}
