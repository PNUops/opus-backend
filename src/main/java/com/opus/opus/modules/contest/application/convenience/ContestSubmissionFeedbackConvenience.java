package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionFeedbackConvenience {

    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;

    public List<ContestSubmissionFeedback> getFeedbacksByTeamSubmissionItem(final Long submissionItemId,
                                                                            final Long teamId) {
        return contestSubmissionFeedbackRepository.findAllBySubmissionItemIdAndTeamIdOrderByIdDesc(submissionItemId,
                teamId);
    }

    public ContestSubmissionFeedback getValidateFeedback(final Long submissionItemId, final Long teamId,
                                                         final Long memberId) {
        return findFeedback(submissionItemId, teamId, memberId)
                .orElseThrow(() -> new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));
    }

    public ContestSubmissionFeedback getValidateFeedbackInTeamSubmissionItem(final Long feedbackId,
                                                                             final Long submissionItemId,
                                                                             final Long teamId) {
        return contestSubmissionFeedbackRepository.findByIdAndSubmissionItemIdAndTeamId(feedbackId, submissionItemId,
                        teamId)
                .orElseThrow(() -> new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));
    }

    public Optional<ContestSubmissionFeedback> findFeedback(final Long submissionItemId, final Long teamId,
                                                            final Long memberId) {
        return contestSubmissionFeedbackRepository.findBySubmissionItemIdAndTeamIdAndMemberId(submissionItemId, teamId,
                memberId);
    }
}
