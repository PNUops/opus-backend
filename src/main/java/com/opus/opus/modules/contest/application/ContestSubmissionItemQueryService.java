package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemSummaryResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionItemQueryService {

    private static final String SCHEDULED = "SCHEDULED";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String CLOSED = "CLOSED";

    private final ContestSubmissionItemRepository contestSubmissionItemRepository;

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    public List<ContestSubmissionItemSummaryResponse> getSubmissionItems(final Long contestId, final String status) {
        contestConvenience.validateExistContest(contestId);
        final List<ContestSubmissionItem> submissionItems =
                contestSubmissionItemRepository.findAllByContestIdOrderByUpdatedAtDesc(contestId);
        return toSummaryResponses(submissionItems).stream()
                .filter(response -> matchesStatus(response, status))
                .toList();
    }

    public ContestSubmissionItemResponse getSubmissionItem(final Long contestId, final Long submissionItemId,
                                                           final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamMemberConvenience.validateTeamMemberInContestUnlessAdmin(contestId, member);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        return ContestSubmissionItemResponse.from(submissionItem);
    }

    private List<ContestSubmissionItemSummaryResponse> toSummaryResponses(
            final List<ContestSubmissionItem> submissionItems) {
        final LocalDateTime now = LocalDateTime.now();
        return submissionItems.stream()
                .map(submissionItem -> ContestSubmissionItemSummaryResponse.of(
                        submissionItem, resolveOperationStatus(submissionItem, now)))
                .toList();
    }

    private String resolveOperationStatus(final ContestSubmissionItem submissionItem, final LocalDateTime now) {
        if (now.isBefore(submissionItem.getStartAt())) {
            return SCHEDULED;
        }
        if (now.isAfter(submissionItem.getEndAt())) {
            return CLOSED;
        }
        return IN_PROGRESS;
    }

    private boolean matchesStatus(final ContestSubmissionItemSummaryResponse response, final String status) {
        return status == null || status.isBlank() || status.equals(response.operationStatus());
    }
}
