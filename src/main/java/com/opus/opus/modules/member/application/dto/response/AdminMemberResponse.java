package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.MemberStatus;
import java.time.LocalDateTime;

public record AdminMemberResponse(

        Long memberId,

        String name,

        String studentId,

        String email,

        MemberRoleType roleType,

        MemberStatus status,

        LocalDateTime createdAt
) {
    public static AdminMemberResponse from(final Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentId(),
                member.getEmail(),
                member.getPrimaryRole(),
                MemberStatus.from(member),
                member.getCreatedAt()
        );
    }
}
