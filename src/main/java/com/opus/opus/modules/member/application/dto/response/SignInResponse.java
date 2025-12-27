package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.util.Set;

public record SignInResponse(

        Long memberId,

        String name,

        String token,

        Set<MemberRoleType> types
) {
    public static SignInResponse from(final Member member, final String token) {
        return new SignInResponse(
                member.getId(),
                member.getName(),
                token,
                member.getRoles()
        );
    }
}

