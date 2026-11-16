package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_TO_VIEW_SUBMISSION;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.FileFeedbackCommandService;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.event.SubmissionFeedbackNotificationEvent;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
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

    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;

    private final ContestConvenience contestConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestSubmissionFeedbackConvenience contestSubmissionFeedbackConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    private final FileFeedbackCommandService fileFeedbackCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public void saveFeedback(final Long contestId, final Long teamId, final Long submissionItemId,
                             final Long memberId, final String description, final List<MultipartFile> files,
                             final List<Long> removeFileIds) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        contestMemberConvenience.validateAssignedTeam(contestId, memberId, teamId);
        validateStaffViewableSubmissionItem(submissionItem);
        // 피드백은 제출 항목·팀 단위로 남지만, 작성은 팀의 제출물이 있을 때만 허용한다 (#257)
        final ContestSubmission submission =
                contestSubmissionConvenience.getValidateExistSubmission(teamId, submissionItem);

        final ContestSubmissionFeedback feedback = upsertFeedback(contestId, submission, memberId, description);

        fileFeedbackCommandService.deleteFeedbackFiles(removeFileIds, feedback.getId());
        fileFeedbackCommandService.storeFeedbackFiles(files, feedback.getId());
    }

    public void markFeedbackAsRead(final Long contestId, final Long teamId, final Long submissionItemId,
                                   final Long feedbackId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        final ContestSubmissionFeedback feedback = contestSubmissionFeedbackConvenience
                .getValidateFeedbackInTeamSubmissionItem(feedbackId, submissionItemId, teamId);
        feedback.markAsRead();
    }

    private void validateStaffViewableSubmissionItem(final ContestSubmissionItem submissionItem) {
        if (!submissionItem.isStaffViewable()) {
            throw new ContestException(NOT_ALLOWED_TO_VIEW_SUBMISSION);
        }
    }

    private ContestSubmissionFeedback upsertFeedback(final Long contestId, final ContestSubmission submission,
                                                     final Long memberId, final String description) {
        return contestSubmissionFeedbackConvenience.findFeedback(submission.getSubmissionItem().getId(),
                        submission.getTeamId(), memberId)
                .map(existing -> updateFeedback(existing, description))
                .orElseGet(() -> createFeedback(contestId, submission, memberId, description));
    }

    private ContestSubmissionFeedback updateFeedback(final ContestSubmissionFeedback feedback,
                                                     final String description) {
        feedback.updateDescription(description);
        return feedback;
    }

    private ContestSubmissionFeedback createFeedback(final Long contestId, final ContestSubmission submission,
                                                     final Long memberId, final String description) {
        final ContestSubmissionFeedback feedback = contestSubmissionFeedbackRepository.save(
                ContestSubmissionFeedback.builder()
                        .description(description)
                        .memberId(memberId)
                        .teamId(submission.getTeamId())
                        .submissionItem(submission.getSubmissionItem())
                        .build());
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
