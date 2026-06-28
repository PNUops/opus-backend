package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.DownloadSubmissionRow;
import com.opus.opus.modules.contest.domain.dao.DownloadTargetResult;
import com.opus.opus.modules.contest.exception.ContestException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionConvenience {

    private final ContestSubmissionRepository contestSubmissionRepository;

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
            throw new ContestException(INVALID_SUBMISSION_FOR_CONTEST);
        }
        return submission;
    }

    public boolean isSubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        return contestSubmissionRepository.existsByTeamIdAndSubmissionItem(teamId, submissionItem);
    }

    public List<DownloadTargetResult> getDownloadTargets(final Long contestId, final Long submissionTypeId, final Long trackId) {
        return contestSubmissionRepository.findDownloadTargets(contestId, submissionTypeId, trackId);
    }

    public List<DownloadSubmissionRow> getDownloadSubmissions(final Long contestId) {
        return contestSubmissionRepository.findDownloadSubmissions(contestId);
    }
}
