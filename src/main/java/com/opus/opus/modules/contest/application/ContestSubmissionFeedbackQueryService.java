package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.FileFeedbackQueryService;
import com.opus.opus.modules.file.application.convenience.FileFeedbackConvenience;
import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
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

    public List<ContestSubmissionFeedbackResponse> getFeedbacks(final Long contestId, final Long submissionId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final List<ContestSubmissionFeedback> feedbacks =
                contestSubmissionFeedbackConvenience.getFeedbacksBySubmission(submissionId);

        final Map<Long, Member> memberMap = memberConvenience.findAllById(feedbacks.stream()
                        .map(ContestSubmissionFeedback::getMemberId)
                        .distinct()
                        .toList())
                .stream()
                .collect(toMap(Member::getId, member -> member));

        final Map<Long, List<FeedbackFileInfo>> filesByFeedbackId = fileFeedbackConvenience.findFilesGroupedByFeedbackIds(
                feedbacks.stream()
                        .map(ContestSubmissionFeedback::getId)
                        .toList());

        return feedbacks.stream()
                .map(feedback -> ContestSubmissionFeedbackResponse.of(feedback, memberMap.get(feedback.getMemberId()),
                        filesByFeedbackId.getOrDefault(feedback.getId(), List.of())))
                .toList();
    }

    public ContestSubmissionMyFeedbackResponse getFeedback(final Long contestId, final Long submissionId,
                                                             final Long memberId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedback(submissionId, memberId);

        final List<FeedbackFileInfo> files = fileFeedbackConvenience
                .findFilesGroupedByFeedbackIds(List.of(feedback.getId()))
                .getOrDefault(feedback.getId(), List.of());

        return ContestSubmissionMyFeedbackResponse.of(feedback, files);
    }

    public FileDownload downloadFeedbackFile(final Long contestId, final Long submissionId, final Long feedbackId,
                                             final Long fileId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedbackInSubmission(feedbackId, submissionId);

        return fileFeedbackQueryService.download(feedback.getId(), fileId);
    }
}
