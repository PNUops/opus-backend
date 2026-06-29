package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionStatusResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.UpcomingSubmissionResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionSummaryResult;
import com.opus.opus.modules.contest.domain.dao.TeamSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.UpcomingSubmissionResult;
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
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();

        final Team team = teamConvenience.getValidateTeamInContest(submission.getTeamId(), contestId);
        // 학생은 해당 팀의 팀장/팀원만 조회할 수 있다. (관리자/교수/직원/외부멘토는 제한 없음)
        teamMemberConvenience.validateTeamMemberIfStudent(team.getId(), member);

        final String trackName = submissionItem.getTrackName();
        final List<FileDocument> fileDocuments = fileDocumentConvenience.findAllBySubmissionId(submissionId);

        /* TODO
        제출물 코멘트 기능이 아직 없어서, 제출물 카운트를 0으로 고정하였습니다.
        코멘트 기능 추가 시 실제 개수로 교체가 필요합니다.
         */
        final int commentCount = 0;

        final SubmissionStatus status = submission.isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;
        return ContestSubmissionDetailResponse.of(submission, team, trackName, fileDocuments, commentCount, status);
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
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberIfStudent(teamId, member);

        final long totalItemCount = contestSubmissionItemRepository.countByContestId(contestId);
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
