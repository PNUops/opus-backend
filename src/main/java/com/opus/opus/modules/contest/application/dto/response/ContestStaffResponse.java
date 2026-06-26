package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record ContestStaffResponse(
        Long contestMemberId,
        Long memberId,
        String name,
        String email,
        String roleType,
        List<TeamInfo> teams
) {
    public static ContestStaffResponse of(final ContestMember contestMember, final Member member,
                                          final List<Team> teams, final String roleType) {
        return new ContestStaffResponse(
                contestMember.getId(),
                member.getId(),
                member.getName(),
                member.getEmail(),
                roleType,
                teams.stream().map(TeamInfo::from).toList()
        );
    }

    public record TeamInfo(
            Long teamId,
            String teamName
    ) {
        public static TeamInfo from(final Team team) {
            return new TeamInfo(team.getId(), team.getTeamName());
        }
    }
}
