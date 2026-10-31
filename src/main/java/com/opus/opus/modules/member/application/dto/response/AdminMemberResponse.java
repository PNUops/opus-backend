package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.member.application.MemberStatus;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record AdminMemberResponse(

        Long memberId,

        String name,

        String studentId,

        String email,

        List<MemberRoleType> roles,

        MemberStatus status,

        LocalDateTime createdAt
) {
    public static AdminMemberResponse from(final Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentId(),
                member.getEmail(),
                member.getRoles().stream()
                        .sorted(Comparator.comparingLong(MemberRoleType::getId))
                        .toList(),
                MemberStatus.from(member),
                member.getCreatedAt()
        );
    }
}
