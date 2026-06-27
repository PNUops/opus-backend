package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_MEMBER_TYPE;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestMemberQueryService {

    private final ContestMemberRepository contestMemberRepository;
    private final ContestConvenience contestConvenience;
    private final MemberConvenience memberConvenience;
    private final TeamConvenience teamConvenience;

    public List<ContestStaffResponse> getAssignedStaff(final Long contestId, final String memberType,
                                                       final String search) {
        contestConvenience.validateExistContest(contestId);
        final List<ContestMember> contestMembers = contestMemberRepository.findAllByContestId(contestId);
        return filterStaff(toResponses(contestMembers), memberType, search);
    }

    private List<ContestStaffResponse> toResponses(final List<ContestMember> contestMembers) {
        final Map<Long, Member> members = memberConvenience.getMembersByIds(extractMemberIds(contestMembers));
        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(extractTeamIds(contestMembers));
        return contestMembers.stream()
                .map(contestMember -> toResponse(contestMember, members, teams))
                .filter(Objects::nonNull)
                .toList();
    }

    private ContestStaffResponse toResponse(final ContestMember contestMember, final Map<Long, Member> members,
                                            final Map<Long, Team> teams) {
        final Member member = members.get(contestMember.getMemberId());
        if (member == null) {
            return null;
        }
        return ContestStaffResponse.of(contestMember, member, mapTeams(contestMember, teams), resolveRoleType(member));
    }

    private List<Team> mapTeams(final ContestMember contestMember, final Map<Long, Team> teams) {
        return contestMember.getTeamIds().stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private String resolveRoleType(final Member member) {
        return member.getRoles().stream()
                .filter(MemberRoleType::isStaff)
                .map(Enum::name)
                .findFirst()
                .orElse(null);
    }

    private List<ContestStaffResponse> filterStaff(final List<ContestStaffResponse> staff, final String memberType,
                                                   final String search) {
        final MemberRoleType roleType = parseMemberType(memberType);
        return staff.stream()
                .filter(response -> matchesRole(response, roleType))
                .filter(response -> matchesSearch(response, search))
                .toList();
    }

    private boolean matchesRole(final ContestStaffResponse staff, final MemberRoleType roleType) {
        return roleType == null || roleType.name().equals(staff.roleType());
    }

    private boolean matchesSearch(final ContestStaffResponse staff, final String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        final String keyword = search.toLowerCase();
        return staff.name().toLowerCase().contains(keyword) || matchesTeamName(staff, keyword);
    }

    private boolean matchesTeamName(final ContestStaffResponse staff, final String keyword) {
        return staff.teams().stream()
                .anyMatch(team -> team.teamName().toLowerCase().contains(keyword));
    }

    private MemberRoleType parseMemberType(final String memberType) {
        if (memberType == null || memberType.isBlank()) {
            return null;
        }
        try {
            return MemberRoleType.valueOf(memberType);
        } catch (final IllegalArgumentException exception) {
            throw new ContestMemberException(INVALID_MEMBER_TYPE);
        }
    }

    private List<Long> extractMemberIds(final List<ContestMember> contestMembers) {
        return contestMembers.stream()
                .map(ContestMember::getMemberId)
                .toList();
    }

    private List<Long> extractTeamIds(final List<ContestMember> contestMembers) {
        return contestMembers.stream()
                .flatMap(contestMember -> contestMember.getTeamIds().stream())
                .distinct()
                .toList();
    }
}
