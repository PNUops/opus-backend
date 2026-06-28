package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.FileFeedbackCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestSubmissionFeedbackCommandService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestSubmissionFeedbackConvenience contestSubmissionFeedbackConvenience;
    private final FileFeedbackCommandService fileFeedbackCommandService;

    public void saveFeedback(final Long contestId, final Long submissionId, final Long memberId,
                             final String description, final List<MultipartFile> files,
                             final List<Long> removeFileIds) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateSubmissionInContest(contestId, submissionId);

        final ContestSubmissionFeedback feedback = contestSubmissionFeedbackConvenience.upsertFeedback(submission, memberId, description);

        fileFeedbackCommandService.deleteFeedbackFiles(removeFileIds, feedback.getId());
        fileFeedbackCommandService.storeFeedbackFiles(files, feedback.getId());
    }
}
