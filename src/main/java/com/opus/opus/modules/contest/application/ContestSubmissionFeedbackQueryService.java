package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionFeedbackConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.file.application.FileFeedbackQueryService;
import com.opus.opus.modules.file.application.convenience.FileFeedbackConvenience;
import com.opus.opus.modules.file.application.dto.FeedbackFileInfo;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
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
    private final ContestSubmissionFeedbackConvenience contestSubmissionFeedbackConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final MemberConvenience memberConvenience;
    private final FileFeedbackConvenience fileFeedbackConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    private final FileFeedbackQueryService fileFeedbackQueryService;

    public List<ContestSubmissionFeedbackResponse> getFeedbacks(final Long contestId, final Long teamId,
                                                                final Long submissionItemId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final List<ContestSubmissionFeedback> feedbacks =
                contestSubmissionFeedbackConvenience.getFeedbacksByTeamSubmissionItem(submissionItemId, teamId);

        final Map<Long, Member> memberMap = findMembersByFeedbacks(feedbacks);
        final Map<Long, List<FeedbackFileInfo>> filesByFeedbackId = groupFilesByFeedbacks(feedbacks);

        return feedbacks.stream()
                .map(feedback -> ContestSubmissionFeedbackResponse.of(feedback, memberMap.get(feedback.getMemberId()),
                        filesByFeedbackId.getOrDefault(feedback.getId(), List.of())))
                .toList();
    }

    public ContestSubmissionMyFeedbackResponse getFeedback(final Long contestId, final Long teamId,
                                                           final Long submissionItemId, final Long memberId) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);

        final ContestSubmissionFeedback feedback =
                contestSubmissionFeedbackConvenience.getValidateFeedback(submissionItemId, teamId, memberId);

        final List<FeedbackFileInfo> files = fileFeedbackConvenience
                .findFilesGroupedByFeedbackIds(List.of(feedback.getId()))
                .getOrDefault(feedback.getId(), List.of());

        return ContestSubmissionMyFeedbackResponse.of(feedback, files);
    }

    public FileDownload downloadFeedbackFile(final Long contestId, final Long teamId, final Long submissionItemId,
                                             final Long feedbackId, final Long fileId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionFeedback feedback = contestSubmissionFeedbackConvenience
                .getValidateFeedbackInTeamSubmissionItem(feedbackId, submissionItemId, teamId);

        return fileFeedbackQueryService.download(feedback.getId(), fileId);
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
}
