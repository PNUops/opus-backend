package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import java.util.Set;

public record TeamMemberResponse(
        Long teamMemberId,
        String teamMemberName,
        TeamMemberRoleType roleType
) {
    public static TeamMemberResponse of(final TeamMember teamMember, final Member member) {
        return new TeamMemberResponse(
                member.getId(),
                member.getName(),
                teamMember.getRoles().stream().findFirst().orElse(TeamMemberRoleType.ROLE_팀원)
        );
    }
}
