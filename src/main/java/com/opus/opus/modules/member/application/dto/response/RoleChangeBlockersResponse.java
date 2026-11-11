package com.opus.opus.modules.member.application.dto.response;

import java.util.List;

public record RoleChangeBlockersResponse(
        List<TeamMembership> teams,
        List<StaffAssignment> staffAssignments
) {

    public static RoleChangeBlockersResponse empty() {
        return new RoleChangeBlockersResponse(List.of(), List.of());
    }

    public record TeamMembership(
            Long contestId,
            String contestName,
            Long teamId,
            String teamName,
            String projectName,
            boolean isLeader
    ) {
    }

    public record StaffAssignment(
            Long contestId,
            String contestName,
            Long contestMemberId,
            List<AssignedTeam> teams
    ) {
    }

    public record AssignedTeam(
            Long teamId,
            String teamName,
            String projectName
    ) {
    }
}
