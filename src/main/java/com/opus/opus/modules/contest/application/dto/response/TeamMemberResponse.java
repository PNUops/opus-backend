package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;

public record TeamMemberResponse(
        Long teamMemberId,
        String teamMemberName,
        TeamMemberRoleType roleType
) {
    public static TeamMemberResponse of(final TeamMember teamMember, final Member member) {
        TeamMemberRoleType role = teamMember.getRoles().contains(TeamMemberRoleType.ROLE_팀장)
                ? TeamMemberRoleType.ROLE_팀장
                : TeamMemberRoleType.ROLE_팀원;

        return new TeamMemberResponse(
                member.getId(),
                member.getName(),
                role
        );
    }
}
