package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_ASSIGNED_TEAM;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFileResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectsResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
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
import com.opus.opus.modules.member.domain.Member;
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
public class ContestMentorQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestMemberRepository contestMemberRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;
    private final TeamConvenience teamConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public MentorProjectsResponse getMentorProjects(final Long contestId, final Member mentor) {
        contestConvenience.validateExistContest(contestId);

        final List<Long> teamIds = contestMemberRepository.findByContestIdAndMemberId(contestId, mentor.getId())
                .map(contestMember -> List.copyOf(contestMember.getTeamIds()))
                .orElseGet(List::of);
        if (teamIds.isEmpty()) {
            return new MentorProjectsResponse(0, 0L, List.of());
        }

        final Map<Long, Team> teams = teamConvenience.getTeamsByIds(teamIds);
        final Map<Long, String> trackNames = trackNameMap(contestId);
        final Map<Long, Long> pendingCounts = pendingFeedbackCountsByTeam(contestId, mentor.getId(), teamIds);
        final String roleType = mentor.getStaffRoleName();

        final List<MentorProjectResponse> projects = teamIds.stream()
                .map(teams::get)
                .filter(Objects::nonNull)
                .map(team -> MentorProjectResponse.of(team, trackNames.get(team.getTrackId()), roleType,
                        pendingCounts.getOrDefault(team.getId(), 0L)))
                .toList();

        final long totalPendingCount = projects.stream()
                .mapToLong(MentorProjectResponse::pendingFeedbackCount)
                .sum();
        return new MentorProjectsResponse(projects.size(), totalPendingCount, projects);
    }

    public TeamSubmissionsResponse getTeamSubmissions(final Long contestId, final Long teamId, final Member mentor) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        validateAssignedTeam(contestId, mentor.getId(), teamId);

        final String trackName = trackNameMap(contestId).get(team.getTrackId());

        final List<ContestSubmission> submissions = contestSubmissionRepository.findPublicSubmissionsByTeam(contestId, teamId);
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

    private void validateAssignedTeam(final Long contestId, final Long memberId, final Long teamId) {
        final ContestMember contestMember = contestMemberRepository.findByContestIdAndMemberId(contestId, memberId)
                .orElseThrow(() -> new ContestMemberException(NOT_ASSIGNED_TEAM));
        if (!contestMember.getTeamIds().contains(teamId)) {
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
