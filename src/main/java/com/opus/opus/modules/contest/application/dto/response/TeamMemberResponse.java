package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import java.util.Set;

public record TeamMemberResponse(
        Long memberId,
        String name,
        String studentId,
        Set<TeamMemberRoleType> teamRoles
) {
    public static TeamMemberResponse of(final TeamMember teamMember, final Member member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentId(),
                teamMember.getRoles()
        );
    }
}
