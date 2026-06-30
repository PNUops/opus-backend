package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_TO_VIEW_SUBMISSION;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionStatusResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.UpcomingSubmissionResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionSummaryResult;
import com.opus.opus.modules.contest.domain.dao.TeamSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.UpcomingSubmissionResult;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.convenience.FileDocumentConvenience;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionItemRepository contestSubmissionItemRepository;
    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final FileDocumentConvenience fileDocumentConvenience;

    public ContestSubmissionDetailResponse getSubmissionDetail(final Long contestId, final Long submissionId,
                                                               final Member member) {
        contestConvenience.validateExistContest(contestId);

        final ContestSubmission submission = contestSubmissionConvenience.getValidateSubmissionInContest(contestId,
                submissionId);

        final Team team = teamConvenience.getValidateTeamInContest(submission.getTeamId(), contestId);
        validateSubmissionViewable(submission.getSubmissionItem().getVisibility(), contestId, team.getId(), member);

        final ContestTrack track = team.getTrackId() != null
                ? contestTrackConvenience.getValidateExistTrack(team.getContestId(), team.getTrackId())
                : null;
        final String trackName = track != null ? track.getTrackName() : null;
        final List<FileDocument> fileDocuments = fileDocumentConvenience.findAllBySubmissionId(submissionId);

        final long feedbackCount = contestSubmissionFeedbackRepository.countBySubmissionId(submissionId);

        final SubmissionStatus status = submission.isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;
        return ContestSubmissionDetailResponse.of(submission, team, trackName, fileDocuments, feedbackCount, status);
    }

    public List<ContestSubmissionStatusResponse> getSubmissionStatuses(final Long contestId,
                                                                       final Long submissionItemId,
                                                                       final SubmissionStatus status,
                                                                       final Long trackId, final String keyword) {
        contestConvenience.validateExistContest(contestId);

        final LocalDateTime now = LocalDateTime.now();
        final List<ContestSubmissionStatusResult> results = contestSubmissionConvenience.getSubmissionStatuses(
                contestId, submissionItemId, status, trackId, keyword, now);

        return results.stream()
                .map(result -> ContestSubmissionStatusResponse.of(result,
                        determineSubmissionStatus(result.submissionId(), result.firstSubmittedAt(), result.deadline(),
                                now)))
                .toList();
    }

    public ContestSubmissionSummaryResponse getSubmissionSummary(final Long contestId, final Long submissionItemId,
                                                                 final Long trackId) {
        contestConvenience.validateExistContest(contestId);

        final ContestSubmissionSummaryResult result = contestSubmissionConvenience.getSubmissionSummary(
                contestId, submissionItemId, trackId);

        return ContestSubmissionSummaryResponse.of(result);
    }

    public List<TeamSubmissionItemResponse> getTeamSubmissionStatuses(final Long contestId, final Long teamId,
                                                                      final SubmissionStatus status,
                                                                      final Member member) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        // 학생은 해당 팀의 팀장/팀원만 조회할 수 있다. (관리자는 제한 없음)
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        final LocalDateTime now = LocalDateTime.now();
        final List<TeamSubmissionStatusResult> results = contestSubmissionConvenience.getTeamSubmissionStatuses(
                contestId, teamId, team.getTrackId());

        final List<Map.Entry<TeamSubmissionStatusResult, SubmissionStatus>> matchedResults = results.stream()
                .map(result -> Map.entry(result, determineSubmissionStatus(
                        result.submissionId(), result.firstSubmittedAt(), result.deadlineAt(), now)))
                .filter(entry -> status == null || status == entry.getValue())
                .toList();
        final Map<Long, List<FileDocument>> filesBySubmissionId = findFilesBySubmissionId(
                matchedResults.stream().map(Map.Entry::getKey).toList());

        return matchedResults.stream()
                .map(entry -> TeamSubmissionItemResponse.of(
                        entry.getKey(),
                        filesBySubmissionId.getOrDefault(entry.getKey().submissionId(), List.of()),
                        entry.getValue()))
                .toList();
    }

    public List<UpcomingSubmissionResponse> getUpcomingTeamSubmissions(final Long contestId, final Long teamId,
                                                                       final Member member) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        // 학생은 해당 팀의 팀장/팀원만 조회할 수 있다. (관리자는 제한 없음)
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        final LocalDateTime now = LocalDateTime.now();
        final List<UpcomingSubmissionResult> results = contestSubmissionConvenience.getUpcomingTeamSubmissions(
                contestId, teamId, team.getTrackId(), now);

        return results.stream()
                .map(result -> UpcomingSubmissionResponse.of(result,
                        determineSubmissionStatus(result.submissionId(), result.firstSubmittedAt(),
                                result.deadlineAt(), now)))
                .toList();
    }

    public TeamSubmissionSummaryResponse getTeamSubmissionSummary(final Long contestId, final Long teamId,
                                                                  final Member member) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        final long totalItemCount = contestSubmissionItemRepository.countByContestAndTrack(contestId,
                team.getTrackId());
        final List<ContestSubmission> submissions = contestSubmissionRepository.findAllByTeamIdAndContestId(teamId,
                contestId);
        final long submittedCount = submissions.size();
        final List<Long> submissionIds = submissions.stream().map(ContestSubmission::getId).toList();
        final long totalFeedbackCount = submissionIds.isEmpty() ? 0
                : contestSubmissionFeedbackRepository.countBySubmissionIdIn(submissionIds);

        return new TeamSubmissionSummaryResponse(totalItemCount, submittedCount, totalFeedbackCount);
    }

    public List<ContestSubmissionTimelineResponse> getSubmissionTimeline(final Long contestId, final Long teamId,
                                                                         final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        return contestSubmissionRepository.findAllByTeamIdAndContestId(teamId, contestId)
                .stream()
                .map(ContestSubmissionTimelineResponse::from)
                .toList();
    }

    private void validateSubmissionViewable(final SubmissionVisibility visibility, final Long contestId,
                                            final Long teamId, final Member member) {
        final boolean viewable = switch (visibility) {
            case PUBLIC, MEMBER -> true;
            case STAFF -> member.isAdmin()
                    || teamMemberConvenience.isTeamMember(teamId, member.getId())
                    || contestMemberConvenience.isAssignedTeam(contestId, member.getId(), teamId);
            case TEAM -> member.isAdmin()
                    || teamMemberConvenience.isTeamMember(teamId, member.getId());
        };
        if (!viewable) {
            throw new ContestException(NOT_ALLOWED_TO_VIEW_SUBMISSION);
        }
    }

    private SubmissionStatus determineSubmissionStatus(final Long submissionId, final LocalDateTime firstSubmittedAt,
                                                       final LocalDateTime deadlineAt, final LocalDateTime now) {
        if (submissionId != null) {
            return firstSubmittedAt.isAfter(deadlineAt) ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;
        }
        return now.isAfter(deadlineAt)
                ? SubmissionStatus.NOT_SUBMITTED_AFTER_DEADLINE : SubmissionStatus.NOT_SUBMITTED;
    }

    private Map<Long, List<FileDocument>> findFilesBySubmissionId(final List<TeamSubmissionStatusResult> results) {
        final List<Long> submissionIds = results.stream()
                .map(TeamSubmissionStatusResult::submissionId)
                .filter(Objects::nonNull)
                .toList();
        if (submissionIds.isEmpty()) {
            return new HashMap<>();
        }
        return fileDocumentConvenience.findAllBySubmissionIds(submissionIds).stream()
                .collect(Collectors.groupingBy(FileDocument::getSubmissionId));
    }
}
