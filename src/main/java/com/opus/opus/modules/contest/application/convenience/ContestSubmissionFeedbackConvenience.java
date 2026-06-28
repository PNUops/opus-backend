package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionFeedbackConvenience {

    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;

    public List<ContestSubmissionFeedback> getFeedbacksBySubmission(final Long submissionId) {
        return contestSubmissionFeedbackRepository.findAllBySubmissionIdOrderByIdDesc(submissionId);
    }

    public ContestSubmissionFeedback getValidateFeedback(final Long submissionId, final Long memberId) {
        return contestSubmissionFeedbackRepository.findBySubmissionIdAndMemberId(submissionId, memberId)
                .orElseThrow(() -> new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));
    }

    public ContestSubmissionFeedback getValidateFeedbackInSubmission(final Long feedbackId, final Long submissionId) {
        return contestSubmissionFeedbackRepository.findById(feedbackId)
                .filter(feedback -> feedback.getSubmission().getId().equals(submissionId))
                .orElseThrow(() -> new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));
    }

    public ContestSubmissionFeedback upsert(final ContestSubmission submission, final Long memberId, final String description) {
        return contestSubmissionFeedbackRepository.findBySubmissionIdAndMemberId(submission.getId(), memberId)
                .map(existing -> {
                    existing.updateDescription(description);
                    return existing;
                })
                .orElseGet(() -> contestSubmissionFeedbackRepository.save(ContestSubmissionFeedback.builder()
                        .description(description)
                        .memberId(memberId)
                        .submission(submission)
                        .build()));
    }
}
