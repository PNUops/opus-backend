package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_MEMBER_TYPE;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_ASSIGNED_TEAM;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFileResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorContestResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.TeamPendingFeedbackResult;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.domain.dao.SubmissionFileInfo;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestMemberQueryService {

    private final ContestMemberRepository contestMemberRepository;
    private final ContestConvenience contestConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;
    private final MemberConvenience memberConvenience;
    private final TeamConvenience teamConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public List<ContestStaffResponse> getAssignedStaff(final Long contestId, final String memberType,
                                                       final String search) {
        contestConvenience.validateExistContest(contestId);
        final List<ContestMember> contestMembers = contestMemberRepository.findAllByContestId(contestId);
        return filterStaff(toResponses(contestMembers), memberType, search);
    }

    public List<MentorContestResponse> getMentorContests(final Member mentor) {
        return contestMemberConvenience.getAssignedContestMembers(mentor.getId()).stream()
                .map(contestMember -> buildMentorContestResponse(contestMember, mentor.getId()))
                .toList();
    }

    public List<MentorProjectResponse> getMentorContestTeams(final Long contestId, final Member mentor) {
        contestConvenience.validateExistContest(contestId);
        final ContestMember contestMember =
                contestMemberConvenience.getValidateExistContestMember(contestId, mentor.getId());

        return buildMentorProjectResponses(contestMember, mentor.getId(), mentor.getStaffRoleName());
    }

    public TeamSubmissionsResponse getTeamSubmissions(final Long contestId, final Long teamId, final Member mentor) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        validateAssignedTeam(contestId, mentor.getId(), teamId);

        final String trackName = trackNameMap(contestId).get(team.getTrackId());

        final List<ContestSubmission> submissions =
                contestSubmissionRepository.findStaffViewableSubmissionsByTeam(contestId, teamId);
        if (submissions.isEmpty()) {
            return TeamSubmissionsResponse.of(team, trackName, List.of());
        }

        final List<Long> submissionIds = submissions.stream().map(ContestSubmission::getId).toList();
        final Set<Long> reviewedSubmissionIds = Set.copyOf(
                contestSubmissionFeedbackRepository.findReviewedSubmissionIds(mentor.getId(), submissionIds));
        final Map<Long, List<ContestSubmissionFileResponse>> filesBySubmissionId = filesGroupedBySubmission(submissionIds);

        final List<MentorSubmissionResponse> submissionResponses = submissions.stream()
                .map(submission -> MentorSubmissionResponse.of(submission,
                        reviewedSubmissionIds.contains(submission.getId()),
                        filesBySubmissionId.getOrDefault(submission.getId(), List.of())))
                .toList();

        return TeamSubmissionsResponse.of(team, trackName, submissionResponses);
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
        return ContestStaffResponse.of(contestMember, member, mapTeams(contestMember, teams), member.getStaffRoleName());
    }

    private List<Team> mapTeams(final ContestMember contestMember, final Map<Long, Team> teams) {
        return contestMember.getTeamIds().stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .toList();
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
        final MemberRoleType roleType = toMemberRoleType(memberType);
        if (!roleType.isStaff()) {
            throw new ContestMemberException(INVALID_MEMBER_TYPE);
        }
        return roleType;
    }

    private MemberRoleType toMemberRoleType(final String memberType) {
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

    // 담당 팀들을 기준으로 분과명 목록(중복 제거)·검토 대기 건수 합계·담당 팀 수를 대회 단위로 집계한다.
    private MentorContestResponse buildMentorContestResponse(final ContestMember contestMember, final Long memberId) {
        final Contest contest = contestMember.getContest();
        final String categoryName = contestCategoryConvenience.getValidateExistCategory(contest.getCategoryId())
                .getCategoryName();

        final List<Long> teamIds = List.copyOf(contestMember.getTeamIds());
        if (teamIds.isEmpty()) {
            return MentorContestResponse.of(contest, categoryName, List.of(), 0L, 0);
        }

        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(teamIds);
        final Map<Long, String> trackNames = trackNameMap(contest.getId());
        final List<String> assignedTrackNames = teamIds.stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .map(team -> trackNames.get(team.getTrackId()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        final long totalPendingCount = pendingFeedbackCountsByTeam(contest.getId(), memberId, teamIds).values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        return MentorContestResponse.of(contest, categoryName, assignedTrackNames, totalPendingCount, teams.size());
    }

    // 배정 순서를 유지하며 담당 팀별 응답을 만든다. (존재하지 않는 팀은 제외)
    private List<MentorProjectResponse> buildMentorProjectResponses(final ContestMember contestMember, final Long memberId, final String roleType) {
        final List<Long> teamIds = List.copyOf(contestMember.getTeamIds());
        if (teamIds.isEmpty()) {
            return List.of();
        }

        final Long contestId = contestMember.getContest().getId();
        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(teamIds);
        final Map<Long, String> trackNames = trackNameMap(contestId);
        final Map<Long, Long> pendingCounts = pendingFeedbackCountsByTeam(contestId, memberId, teamIds);

        return teamIds.stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .map(team -> MentorProjectResponse.of(team, trackNames.get(team.getTrackId()), roleType,
                        pendingCounts.getOrDefault(team.getId(), 0L)))
                .toList();
    }

    private void validateAssignedTeam(final Long contestId, final Long memberId, final Long teamId) {
        if (!contestMemberConvenience.isAssignedTeam(contestId, memberId, teamId)) {
            throw new ContestMemberException(NOT_ASSIGNED_TEAM);
        }
    }

    private Map<Long, String> trackNameMap(final Long contestId) {
        return contestTrackConvenience.getValidateExistTracks(contestId).stream()
                .collect(Collectors.toMap(ContestTrack::getId, ContestTrack::getTrackName));
    }

    private Map<Long, Long> pendingFeedbackCountsByTeam(final Long contestId, final Long memberId,
                                                        final List<Long> teamIds) {
        return contestSubmissionRepository.findPendingFeedbackCountsByTeams(contestId, memberId, teamIds).stream()
                .collect(Collectors.toMap(TeamPendingFeedbackResult::teamId,
                        TeamPendingFeedbackResult::pendingFeedbackCount));
    }

    private Map<Long, List<ContestSubmissionFileResponse>> filesGroupedBySubmission(final List<Long> submissionIds) {
        return fileDocumentQueryService.findFilesBySubmissionIds(submissionIds).stream()
                .collect(Collectors.groupingBy(SubmissionFileInfo::submissionId,
                        Collectors.mapping(ContestSubmissionFileResponse::from, Collectors.toList())));
    }
}
