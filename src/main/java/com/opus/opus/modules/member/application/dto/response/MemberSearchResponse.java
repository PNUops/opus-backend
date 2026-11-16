package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.util.Comparator;
import java.util.List;

public record MemberSearchResponse(

        Long memberId,

        String name,

        String email,

        List<MemberRoleType> roles
) {
    public static MemberSearchResponse from(final Member member) {
        return new MemberSearchResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRoles().stream()
                        .sorted(Comparator.comparingLong(MemberRoleType::getId))
                        .toList()
        );
    }
}
