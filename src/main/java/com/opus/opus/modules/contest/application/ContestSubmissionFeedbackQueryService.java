package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.FileFeedbackQueryService;
import com.opus.opus.modules.file.application.convenience.FileFeedbackConvenience;
import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionFeedbackQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestSubmissionFeedbackConvenience contestSubmissionFeedbackConvenience;
    private final MemberConvenience memberConvenience;
    private final FileFeedbackConvenience fileFeedbackConvenience;
    private final FileFeedbackQueryService fileFeedbackQueryService;
    private final TeamMemberConvenience teamMemberConvenience;

    public List<ContestSubmissionFeedbackResponse> getFeedbacks(final Long contestId, final Long submissionId,
                                                                final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission =
                contestSubmissionConvenience.getValidateSubmissionBelongsToContest(contestId, submissionId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        final List<ContestSubmissionFeedback> feedbacks =
                contestSubmissionFeedbackConvenience.getFeedbacksBySubmission(submissionId);

        final Map<Long, Member> memberMap = findMembersByFeedbacks(feedbacks);
        final Map<Long, List<FeedbackFileInfo>> filesByFeedbackId = groupFilesByFeedbacks(feedbacks);

        return feedbacks.stream()
                .map(feedback -> ContestSubmissionFeedbackResponse.of(feedback, memberMap.get(feedback.getMemberId()),
                        filesByFeedbackId.getOrDefault(feedback.getId(), List.of())))
                .toList();
    }

    private Map<Long, Member> findMembersByFeedbacks(final List<ContestSubmissionFeedback> feedbacks) {
        return memberConvenience.findAllById(feedbacks.stream()
                        .map(ContestSubmissionFeedback::getMemberId)
                        .distinct()
                        .toList())
                .stream()
                .collect(toMap(Member::getId, member -> member));
    }

    private Map<Long, List<FeedbackFileInfo>> groupFilesByFeedbacks(final List<ContestSubmissionFeedback> feedbacks) {
        return fileFeedbackConvenience.findFilesGroupedByFeedbackIds(feedbacks.stream()
                .map(ContestSubmissionFeedback::getId)
                .toList());
    }

    public ContestSubmissionMyFeedbackResponse getFeedback(final Long contestId, final Long submissionId,
                                                             final Long memberId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.getValidateSubmissionBelongsToContest(contestId, submissionId);

        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedback(submissionId, memberId);

        final List<FeedbackFileInfo> files = fileFeedbackConvenience
                .findFilesGroupedByFeedbackIds(List.of(feedback.getId()))
                .getOrDefault(feedback.getId(), List.of());

        return ContestSubmissionMyFeedbackResponse.of(feedback, files);
    }

    public FileDownload downloadFeedbackFile(final Long contestId, final Long submissionId, final Long feedbackId,
                                             final Long fileId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission =
                contestSubmissionConvenience.getValidateSubmissionBelongsToContest(contestId, submissionId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedbackInSubmission(feedbackId, submissionId);

        return fileFeedbackQueryService.download(feedback.getId(), fileId);
    }
}
