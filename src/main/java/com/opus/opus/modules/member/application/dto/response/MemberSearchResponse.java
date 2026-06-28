package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;

public record MemberSearchResponse(

        Long memberId,

        String name,

        String email,

        MemberRoleType roleType
) {
    public static MemberSearchResponse from(final Member member) {
        return new MemberSearchResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPrimaryRole()
        );
    }
}
