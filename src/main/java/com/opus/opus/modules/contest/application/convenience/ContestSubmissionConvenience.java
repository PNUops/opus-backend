package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.INVALID_SUBMISSION_FOR_TEAM;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionSummaryResult;
import com.opus.opus.modules.contest.domain.dao.DownloadSubmissionRow;
import com.opus.opus.modules.contest.domain.dao.DownloadTargetResult;
import com.opus.opus.modules.contest.domain.dao.TeamSubmissionStatusResult;
import com.opus.opus.modules.contest.domain.dao.UpcomingSubmissionResult;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionConvenience {

    private final ContestRepository contestRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionItemRepository submissionItemRepository;

    public ContestSubmission getValidatedSubmission(final Long contestId, final Long teamId,
                                                    final Long submissionId) {
        contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));

        final ContestSubmission submission = contestSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION));

        if (!submission.getTeamId().equals(teamId)) {
            throw new ContestSubmissionMemoException(INVALID_SUBMISSION_FOR_TEAM);
        }

        final ContestSubmissionItem submissionItem =
                submissionItemRepository.findById(submission.getSubmissionItem().getId())
                        .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION_ITEM));

        if (!submissionItem.getContest().getId().equals(contestId)) {
            throw new ContestSubmissionMemoException(ContestSubmissionMemoExceptionType.INVALID_SUBMISSION_FOR_CONTEST);
        }

        return submission;
    }

    public ContestSubmission getValidateExistSubmission(final Long submissionId) {
        return contestSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION));
    }

    // 제출물 기능(#160): 대회 소속이 아니면 존재하지 않는 것으로 간주 → 404
    public ContestSubmission getValidateSubmissionInContest(final Long contestId, final Long submissionId) {
        final ContestSubmission submission = getValidateExistSubmission(submissionId);
        if (!submission.isInContest(contestId)) {
            throw new ContestException(NOT_FOUND_SUBMISSION);
        }
        return submission;
    }

    // 피드백 기능(#159): 대회 소속이 아니면 잘못된 요청 → 400
    public ContestSubmission getValidateSubmissionBelongsToContest(final Long contestId, final Long submissionId) {
        final ContestSubmission submission = getValidateExistSubmission(submissionId);
        if (!submission.isInContest(contestId)) {
            throw new ContestException(ContestExceptionType.INVALID_SUBMISSION_FOR_CONTEST);
        }
        return submission;
    }

    public boolean isSubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        return contestSubmissionRepository.existsByTeamIdAndSubmissionItem(teamId, submissionItem);
    }

    public List<DownloadTargetResult> getDownloadTargets(final Long contestId, final Long submissionTypeId,
                                                         final Long trackId) {
        return contestSubmissionRepository.findDownloadTargets(contestId, submissionTypeId, trackId);
    }

    public List<DownloadSubmissionRow> getDownloadSubmissions(final Long contestId) {
        return contestSubmissionRepository.findDownloadSubmissions(contestId);
    }

    public List<ContestSubmissionStatusResult> getSubmissionStatuses(final Long contestId, final Long submissionItemId,
                                                                     final SubmissionStatus status, final Long trackId,
                                                                     final String keyword, final LocalDateTime now) {
        return contestSubmissionRepository.findSubmissionStatuses(contestId, submissionItemId,
                status == null ? null : status.name(), trackId, keyword, now);
    }

    public List<TeamSubmissionStatusResult> getTeamSubmissionStatuses(final Long contestId, final Long teamId,
                                                                      final Long trackId) {
        return contestSubmissionRepository.findTeamSubmissionStatuses(contestId, teamId, trackId);
    }

    public List<UpcomingSubmissionResult> getUpcomingTeamSubmissions(final Long contestId, final Long teamId,
                                                                     final Long trackId, final LocalDateTime now) {
        return contestSubmissionRepository.findUpcomingTeamSubmissions(contestId, teamId, trackId, now);
    }

    public ContestSubmissionSummaryResult getSubmissionSummary(final Long contestId, final Long submissionItemId,
                                                               final Long trackId) {
        return contestSubmissionRepository.findSubmissionSummary(contestId, submissionItemId, trackId);
    }
}
