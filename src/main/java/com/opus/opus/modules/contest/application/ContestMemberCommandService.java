package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.ALREADY_ASSIGNED_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.DUPLICATE_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_MEMBER_TYPE;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_FOUND_CONTEST_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.application.dto.request.StaffTeamUpdateRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.notification.application.event.StaffAssignedNotificationEvent;
import com.opus.opus.modules.notification.application.event.StaffAssignmentTeam;
import com.opus.opus.modules.notification.application.event.StaffPosition;
import com.opus.opus.modules.notification.application.event.StaffUnassignedNotificationEvent;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestMemberCommandService {

    private final ContestMemberRepository contestMemberRepository;
    private final ContestConvenience contestConvenience;
    private final MemberConvenience memberConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ApplicationEventPublisher eventPublisher;

    public void assignStaff(final Long contestId, final StaffBatchAssignRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        validateNoDuplicateMembers(request.memberIds());
        validateMembers(request.memberIds());
        teamConvenience.validateTeamsInContest(contestId, request.teamIds());
        validateNotAlreadyAssigned(contestId, request.memberIds());
        saveAssignments(contest, request);
        publishStaffAssignedNotifications(contestId, request.memberIds(), request.teamIds());
    }

    public void updateAssignedTeams(final Long contestId, final Long contestMemberId,
                                    final StaffTeamUpdateRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestMember contestMember = getContestMember(contestId, contestMemberId);
        teamConvenience.validateTeamsInContest(contestId, request.addTeamIds());
        final Set<Long> previousTeamIds = Set.copyOf(contestMember.getTeamIds());
        contestMember.updateTeams(request.addTeamIds(), request.deleteTeamIds());
        publishAssignedTeamsChangedNotifications(contestId, contestMember, previousTeamIds);
    }

    public void deleteAssignment(final Long contestId, final Long contestMemberId) {
        contestConvenience.validateExistContest(contestId);
        final ContestMember contestMember = getContestMember(contestId, contestMemberId);
        final List<Long> assignedTeamIds = List.copyOf(contestMember.getTeamIds());
        contestMemberRepository.delete(contestMember);
        publishStaffUnassignedNotifications(contestId, List.of(contestMember.getMemberId()), assignedTeamIds);
    }

    private ContestMember getContestMember(final Long contestId, final Long contestMemberId) {
        return contestMemberRepository.findByIdAndContestId(contestMemberId, contestId)
                .orElseThrow(() -> new ContestMemberException(NOT_FOUND_CONTEST_MEMBER));
    }

    private void validateNoDuplicateMembers(final List<Long> memberIds) {
        if (memberIds.size() != Set.copyOf(memberIds).size()) {
            throw new ContestMemberException(DUPLICATE_MEMBER);
        }
    }

    private void validateMembers(final List<Long> memberIds) {
        final Map<Long, Member> members = memberConvenience.getMembersByIds(memberIds);
        memberIds.forEach(memberId -> validateStaffMember(members.get(memberId)));
    }

    private void validateStaffMember(final Member member) {
        if (member == null) {
            throw new MemberException(NOT_FOUND_MEMBER);
        }
        if (!member.hasStaffRole()) {
            throw new ContestMemberException(INVALID_MEMBER_TYPE);
        }
    }

    private void validateNotAlreadyAssigned(final Long contestId, final List<Long> memberIds) {
        memberIds.forEach(memberId -> {
            if (contestMemberRepository.existsByContestIdAndMemberId(contestId, memberId)) {
                throw new ContestMemberException(ALREADY_ASSIGNED_MEMBER);
            }
        });
    }

    private void saveAssignments(final Contest contest, final StaffBatchAssignRequest request) {
        final List<ContestMember> contestMembers = request.memberIds().stream()
                .map(memberId -> toContestMember(contest, memberId, request.teamIds()))
                .toList();
        contestMemberRepository.saveAll(contestMembers);
    }

    private ContestMember toContestMember(final Contest contest, final Long memberId, final List<Long> teamIds) {
        return ContestMember.builder()
                .contest(contest)
                .memberId(memberId)
                .teamIds(teamIds)
                .build();
    }

    private void publishAssignedTeamsChangedNotifications(final Long contestId, final ContestMember contestMember,
                                                          final Set<Long> previousTeamIds) {
        final Set<Long> currentTeamIds = contestMember.getTeamIds();
        final List<Long> addedTeamIds = currentTeamIds.stream()
                .filter(teamId -> !previousTeamIds.contains(teamId))
                .toList();
        final List<Long> removedTeamIds = previousTeamIds.stream()
                .filter(teamId -> !currentTeamIds.contains(teamId))
                .toList();
        publishStaffAssignedNotifications(contestId, List.of(contestMember.getMemberId()), addedTeamIds);
        publishStaffUnassignedNotifications(contestId, List.of(contestMember.getMemberId()), removedTeamIds);
    }

    private void publishStaffAssignedNotifications(final Long contestId, final List<Long> staffIds,
                                                   final List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return;
        }
        final List<StaffAssignmentTeam> teams = toStaffAssignmentTeams(teamIds);
        memberConvenience.getMembersByIds(staffIds).values().forEach(staff -> toStaffPositions(staff).forEach(
                position -> eventPublisher.publishEvent(new StaffAssignedNotificationEvent(
                        contestId, staff.getId(), staff.getName(), position, teams))));
    }

    private void publishStaffUnassignedNotifications(final Long contestId, final List<Long> staffIds,
                                                     final List<Long> teamIds) {
        if (teamIds.isEmpty()) {
            return;
        }
        final List<StaffAssignmentTeam> teams = toStaffAssignmentTeams(teamIds);
        memberConvenience.getMembersByIds(staffIds).values().forEach(staff -> toStaffPositions(staff).forEach(
                position -> eventPublisher.publishEvent(new StaffUnassignedNotificationEvent(
                        contestId, staff.getId(), staff.getName(), position, teams))));
    }

    private List<StaffAssignmentTeam> toStaffAssignmentTeams(final List<Long> teamIds) {
        return teamConvenience.getTeamsByIds(teamIds).values().stream()
                .map(team -> new StaffAssignmentTeam(team.getId(), toTeamDisplayName(team),
                        teamMemberConvenience.findRealMemberIdsByTeamId(team.getId())))
                .toList();
    }

    private String toTeamDisplayName(final Team team) {
        return team.getTeamName() != null ? team.getTeamName() : team.getProjectName();
    }

    private List<StaffPosition> toStaffPositions(final Member staff) {
        final List<StaffPosition> positions = new ArrayList<>();
        if (staff.isProfessor()) {
            positions.add(StaffPosition.ADVISOR);
        }
        if (staff.isExternalMentor()) {
            positions.add(StaffPosition.MENTOR);
        }
        return positions;
    }
}
