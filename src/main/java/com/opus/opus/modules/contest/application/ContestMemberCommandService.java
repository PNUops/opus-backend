package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.ALREADY_ASSIGNED_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.DUPLICATE_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_MEMBER_TYPE;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_TEAM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_FOUND_CONTEST_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;

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
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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

    public void assignStaff(final Long contestId, final StaffBatchAssignRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        validateNoDuplicateMembers(request.memberIds());
        validateMembers(request.memberIds());
        validateTeams(contestId, request.teamIds());
        validateNotAlreadyAssigned(contestId, request.memberIds());
        saveAssignments(contest, request);
    }

    public void updateAssignedTeams(final Long contestId, final Long contestMemberId,
                                    final StaffTeamUpdateRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestMember contestMember = getContestMember(contestId, contestMemberId);
        validateTeams(contestId, request.addTeamIds());
        contestMember.updateTeams(request.addTeamIds(), request.deleteTeamIds());
    }

    public void deleteAssignment(final Long contestId, final Long contestMemberId) {
        contestConvenience.validateExistContest(contestId);
        final ContestMember contestMember = getContestMember(contestId, contestMemberId);
        contestMemberRepository.delete(contestMember);
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

    private void validateTeams(final Long contestId, final List<Long> teamIds) {
        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(teamIds);
        teamIds.forEach(teamId -> validateTeamInContest(contestId, teams.get(teamId)));
    }

    private void validateTeamInContest(final Long contestId, final Team team) {
        if (team == null) {
            throw new TeamException(NOT_FOUND_TEAM);
        }
        if (!team.getContestId().equals(contestId)) {
            throw new ContestMemberException(INVALID_TEAM_FOR_CONTEST);
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
}
