package com.opus.opus.modules.member.application;

import static com.opus.opus.modules.file.domain.FileImageType.PROFILE;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.MEMBER;

import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.modules.file.application.convenience.FileImageConvenience;
import com.opus.opus.modules.file.domain.FileImage;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_ORDER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_MEMBER_STATUS;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_ROLE_TYPE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_SORT_VALUE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_ASSIGNABLE_ROLE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_WITHDRAWN_MEMBER;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.request.SearchConditionRequest;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.AdminMemberResponse;
import com.opus.opus.modules.member.application.dto.response.AdminMemberSummaryResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.application.dto.response.MemberSearchResponse;
import com.opus.opus.modules.member.application.dto.response.MemberWithdrawalResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.AssignedTeam;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.StaffAssignment;
import com.opus.opus.modules.member.application.dto.response.RoleChangeBlockersResponse.TeamMembership;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.MemberWithdrawalHistory;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.domain.dao.MemberWithdrawalHistoryRepository;
import com.opus.opus.modules.member.domain.dao.MyVoteResponse;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.MyProjectFlatResult;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import com.opus.opus.modules.file.application.dto.FileResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private static final int LIKE_PREVIEW_SIZE = 3;
    private static final int SEARCH_RESULT_LIMIT = 20;

    private final MemberConvenience memberConvenience;
    private final MemberRepository memberRepository;
    private final MemberWithdrawalHistoryRepository memberWithdrawalHistoryRepository;
    private final FileImageConvenience fileImageConvenience;
    private final FileQueryService fileQueryService;
    private final TeamCommentRepository teamCommentRepository;
    private final TeamLikeRepository teamLikeRepository;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamVoteRepository teamVoteRepository;

    private final ContestConvenience contestConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    public EmailFindResponse getMyEmail(final String studentId) {
        final Member member = memberConvenience.getValidateExistMemberByStudentId(studentId);
        return new EmailFindResponse(maskEmail(member.getEmail()));
    }

    private String maskEmail(final String email) {
        final int atIndex = email.indexOf('@');
        final String local = email.substring(0, atIndex);
        final String domain = email.substring(atIndex);
        return maskLocal(local) + domain;
    }

    private String maskLocal(final String local) {
        final int length = local.length();
        // 이메일 로컬 파트 마스킹
        if (length == 1) {
            // 1자: * (ex. a → *)
            return "*";
        }
        if (length <= 3) {
            // 2~3자: 첫 1자리 노출 (ex. ab → a*, abc → a**)
            return local.charAt(0) + "*".repeat(length - 1);
        }
        // 4자 이상: 첫 3자리 노출, 나머지 문자는 * 처리 (ex. abcde → abd**)
        return local.substring(0, 3) + "*".repeat(length - 3);
    }

    public ImageResponse getProfileImage(final Member member) {
        final FileImage profileFile = fileImageConvenience.findByReferenceIdAndReferenceTypeAndImageType(member.getId(), MEMBER, PROFILE);
        final FileResource storageResult = fileQueryService.findFileAndType(profileFile.getFile().getId());
        return new ImageResponse(storageResult.resource(), storageResult.mimeType());
    }

    public AccountInfoResponse getAccountInfo(final Long memberId) {
        final Member member = memberConvenience.getValidateExistMember(memberId);
        return AccountInfoResponse.from(member);
    }

    public List<MemberSearchResponse> getMembersByKeyword(final String keyword, final String roleType) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        final MemberRoleType parsedRoleType = parseRoleType(roleType);
        final Pageable pageable = PageRequest.of(0, SEARCH_RESULT_LIMIT);
        return memberRepository.searchByEmailPrefix(escapeLikePattern(keyword), parsedRoleType, pageable).stream()
                .map(MemberSearchResponse::from)
                .toList();
    }

    public Page<AdminMemberResponse> getAdminMembers(final String keyword, final String roleType, final String status,
                                                     final int page, final int size) {
        final MemberRoleType parsedRoleType = parseRoleType(roleType);
        final Boolean isDeleted = parseIsDeleted(status);
        final String escapedKeyword = (keyword == null || keyword.isBlank()) ? null : escapeLikePattern(keyword.strip());
        final String roleName = parsedRoleType == null ? null : parsedRoleType.name();
        return memberRepository.findAdminMembers(escapedKeyword, roleName, isDeleted, PageRequest.of(page, size))
                .map(AdminMemberResponse::from);
    }

    public AdminMemberSummaryResponse getAdminMemberSummary() {
        return new AdminMemberSummaryResponse(
                memberRepository.countNonFakeMembersByIsDeleted(false),
                memberRepository.countNonFakeMembersByIsDeleted(true)
        );
    }

    public MemberWithdrawalResponse getWithdrawal(final Long memberId) {
        final Member member = memberConvenience.findByIdIncludingDeleted(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        if (!member.getIsDeleted()) {
            throw new MemberException(NOT_WITHDRAWN_MEMBER);
        }
        final MemberWithdrawalHistory history = memberWithdrawalHistoryRepository
                .findTopByMemberIdOrderByIdDesc(memberId)
                .orElse(null);
        // 수행 관리자가 이후 탈퇴했더라도 이름을 보여주기 위해 탈퇴 회원까지 포함해 조회한다.
        final Member admin = (history == null || history.getAdminId() == null)
                ? null
                : memberConvenience.findByIdIncludingDeleted(history.getAdminId()).orElse(null);
        return MemberWithdrawalResponse.of(member, history, admin);
    }

    // 새 역할로는 유지할 수 없는 진행 중 대회의 배정을 반환한다.
    // 학생이 되면 교수·외부멘토 담당 배정이, 교수·외부멘토가 되면 팀 소속이 역할 변경을 막는다.
    public RoleChangeBlockersResponse getRoleChangeBlockers(final Long memberId, final String newRole) {
        final MemberRoleType parsedNewRole = parseAssignableRole(newRole);
        memberConvenience.getValidateExistMember(memberId);

        final Map<Long, String> currentContestNames = contestConvenience.getCurrentContests().stream()
                .collect(toMap(Contest::getId, Contest::getContestName));
        if (currentContestNames.isEmpty()) {
            return RoleChangeBlockersResponse.empty();
        }

        final List<Long> currentContestIds = List.copyOf(currentContestNames.keySet());
        if (parsedNewRole.isStaff()) {
            return new RoleChangeBlockersResponse(
                    findTeamMemberships(memberId, currentContestIds, currentContestNames), List.of());
        }
        return new RoleChangeBlockersResponse(
                List.of(), findStaffAssignments(memberId, currentContestIds, currentContestNames));
    }

    private MemberRoleType parseAssignableRole(final String role) {
        final MemberRoleType parsedRole = parseRoleType(role);
        if (parsedRole == null) {
            throw new MemberException(INVALID_ROLE_TYPE);
        }
        if (!parsedRole.isAdminAssignable()) {
            throw new MemberException(NOT_ASSIGNABLE_ROLE);
        }
        return parsedRole;
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

    private Boolean parseIsDeleted(final String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return MemberStatus.valueOf(status) == MemberStatus.WITHDRAWN;
        } catch (final IllegalArgumentException e) {
            throw new MemberException(INVALID_MEMBER_STATUS);
        }
    }

    // keyword에 포함된 LIKE 와일드카드(%, _)와 ESCAPE 문자(\)를 이스케이프해 리터럴 접두사 매칭을 보장한다.
    private String escapeLikePattern(final String keyword) {
        return keyword.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private MemberRoleType parseRoleType(final String roleType) {
        if (roleType == null || roleType.isBlank()) {
            return null;
        }
        try {
            return MemberRoleType.valueOf(roleType);
        } catch (final IllegalArgumentException e) {
            throw new MemberException(INVALID_ROLE_TYPE);
        }
    }

    public List<MyProjectResponse> getMyProjects(final Long memberId) {
        return teamMemberRepository.findMyProjectsWithAwards(memberId).stream()
                .collect(groupingBy(MyProjectFlatResult::teamId, LinkedHashMap::new, toList()))
                .values().stream()
                .map(MyProjectResponse::from)
                .toList();
    }

    public List<MyVoteResponse> getMyVotes(final Long memberId) {
        return teamVoteRepository.findMyVotes(memberId);
    }

    public Page<MyCommentResponse> getMyComments(final Long memberId, final String sort,
                                                 final LocalDate startDate, final LocalDate endDate,
                                                 final int page, final int size) {
        validateDateRange(startDate, endDate);
        final SearchConditionRequest condition = createSearchCondition(sort, startDate, endDate, page, size);
        return teamCommentRepository.findMyComments(memberId, condition.startDateTime(), condition.endDateTime(),
                        condition.pageable())
                .map(MyCommentResponse::from);
    }

    public List<MyLikePreviewResponse> getMyLikePreview(final Long memberId) {
        final Pageable pageable = PageRequest.of(0, LIKE_PREVIEW_SIZE);
        return teamLikeRepository.findMyRecentLikedProjects(memberId, pageable).stream()
                .map(MyLikePreviewResponse::from)
                .toList();
    }

    public Page<MyLikedProjectResponse> getMyLikedProjects(final Long memberId, final String sort,
                                                           final LocalDate startDate, final LocalDate endDate,
                                                           final Long categoryId, final Long contestId,
                                                           final int page, final int size) {
        validateDateRange(startDate, endDate);
        final SearchConditionRequest searchCondition = createSearchCondition(sort, startDate, endDate, page, size);
        return teamLikeRepository.findMyLikedProjects(memberId, searchCondition.startDateTime(),
                        searchCondition.endDateTime(), categoryId, contestId, searchCondition.pageable())
                .map(MyLikedProjectResponse::from);
    }

    private SearchConditionRequest createSearchCondition(final String sort, final LocalDate startDate,
                                                         final LocalDate endDate, final int page, final int size) {
        final Sort.Direction direction = parseSortDirection(sort);
        final Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
        final LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        final LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        return new SearchConditionRequest(pageable, startDateTime, endDateTime);
    }

    private Sort.Direction parseSortDirection(final String sort) {
        if (sort == null || "latest".equalsIgnoreCase(sort)) {
            return DESC;
        }
        if ("oldest".equalsIgnoreCase(sort)) {
            return ASC;
        }
        throw new MemberException(INVALID_SORT_VALUE);
    }

    private void validateDateRange(final LocalDate startDate, final LocalDate endDate) {
        if ((startDate == null) != (endDate == null)) {
            throw new MemberException(INVALID_DATE_RANGE);
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new MemberException(INVALID_DATE_ORDER);
        }
    }

}
