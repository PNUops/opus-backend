package com.opus.opus.modules.member.application.convenience;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.AssignedTeam;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.StaffAssignment;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.TeamMembership;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleChangeBlockerConvenience {

    private final ContestConvenience contestConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    public RoleChangeBlockersResponse findBlockers(final Long memberId,
                                                   final Set<MemberRoleType> currentRoles,
                                                   final Set<MemberRoleType> targetRoles) {
        final boolean removesStudent = currentRoles.contains(MemberRoleType.ROLE_학생)
                && !targetRoles.contains(MemberRoleType.ROLE_학생);
        final boolean removesLastStaffRole = currentRoles.stream().anyMatch(MemberRoleType::isStaff)
                && targetRoles.stream().noneMatch(MemberRoleType::isStaff);
        if (!removesStudent && !removesLastStaffRole) {
            return RoleChangeBlockersResponse.empty();
        }

        final Map<Long, String> currentContestNames = contestConvenience.getCurrentContests().stream()
                .collect(toMap(Contest::getId, Contest::getContestName));
        if (currentContestNames.isEmpty()) {
            return RoleChangeBlockersResponse.empty();
        }

        final List<Long> currentContestIds = List.copyOf(currentContestNames.keySet());
        return new RoleChangeBlockersResponse(
                removesStudent
                        ? findTeamMemberships(memberId, currentContestIds, currentContestNames)
                        : List.of(),
                removesLastStaffRole
                        ? findStaffAssignments(memberId, currentContestIds, currentContestNames)
                        : List.of());
    }

    private List<TeamMembership> findTeamMemberships(final Long memberId, final List<Long> contestIds,
                                                     final Map<Long, String> contestNames) {
        return teamMemberConvenience.findAllByMemberIdInContests(memberId, contestIds).stream()
                .map(teamMember -> toTeamMembership(teamMember, contestNames))
                .toList();
    }

    private TeamMembership toTeamMembership(final TeamMember teamMember, final Map<Long, String> contestNames) {
        final Team team = teamMember.getTeam();
        return new TeamMembership(team.getContestId(), contestNames.get(team.getContestId()), team.getId(),
                team.getTeamName(), team.getProjectName(),
                teamMember.getRoles().contains(TeamMemberRoleType.ROLE_팀장));
    }

    private List<StaffAssignment> findStaffAssignments(final Long memberId, final List<Long> contestIds,
                                                       final Map<Long, String> contestNames) {
        final List<ContestMember> contestMembers =
                contestMemberConvenience.findAllByMemberIdInContests(memberId, contestIds);
        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(contestMembers.stream()
                .flatMap(contestMember -> contestMember.getTeamIds().stream())
                .distinct()
                .toList());
        return contestMembers.stream()
                .map(contestMember -> toStaffAssignment(contestMember, contestNames, teams))
                .toList();
    }

    private StaffAssignment toStaffAssignment(final ContestMember contestMember, final Map<Long, String> contestNames,
                                              final Map<Long, Team> teams) {
        final Long contestId = contestMember.getContest().getId();
        final List<AssignedTeam> assignedTeams = contestMember.getTeamIds().stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .map(team -> new AssignedTeam(team.getId(), team.getTeamName(), team.getProjectName()))
                .toList();
        return new StaffAssignment(contestId, contestNames.get(contestId), contestMember.getId(), assignedTeams);
    }
}
