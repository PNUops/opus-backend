package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.file.application.FileFeedbackCommandService;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.event.SubmissionFeedbackNotificationEvent;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final TeamMemberConvenience teamMemberConvenience;
    private final ApplicationEventPublisher eventPublisher;

    public void saveFeedback(final Long contestId, final Long submissionId, final Long memberId,
                             final String description, final List<MultipartFile> files,
                             final List<Long> removeFileIds) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateSubmissionBelongsToContest(
                contestId, submissionId);

        final ContestSubmissionFeedback feedback = contestSubmissionFeedbackConvenience.findFeedback(submissionId,
                        memberId)
                .map(existing -> updateFeedback(existing, description))
                .orElseGet(() -> createFeedback(contestId, submission, memberId, description));

        fileFeedbackCommandService.deleteFeedbackFiles(removeFileIds, feedback.getId());
        fileFeedbackCommandService.storeFeedbackFiles(files, feedback.getId());
    }

    public void markFeedbackAsRead(final Long contestId, final Long submissionId, final Long feedbackId,
                                   final Long teamId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.getValidateSubmissionBelongsToContest(contestId, submissionId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedbackInSubmission(feedbackId, submissionId);
        feedback.markAsRead();
    }

    private ContestSubmissionFeedback updateFeedback(final ContestSubmissionFeedback feedback,
                                                     final String description) {
        feedback.updateDescription(description);
        return feedback;
    }

    private ContestSubmissionFeedback createFeedback(final Long contestId, final ContestSubmission submission,
                                                     final Long memberId, final String description) {
        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.createFeedback(submission, memberId, description);
        publishFeedbackNotification(contestId, submission);
        return feedback;
    }

    private void publishFeedbackNotification(final Long contestId, final ContestSubmission submission) {
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();
        final List<Long> memberIds = teamMemberConvenience.findRealMemberIdsByTeamId(submission.getTeamId());
        eventPublisher.publishEvent(new SubmissionFeedbackNotificationEvent(memberIds, contestId,
                submission.getTeamId(), submission.getId(), submissionItem.getId(), submissionItem.getName()));
    }
}
