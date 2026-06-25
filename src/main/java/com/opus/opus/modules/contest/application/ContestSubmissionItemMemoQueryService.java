package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.NOT_FOUND_MEMO;

import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemMemoResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionItemMemo;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemMemoRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionItemMemoQueryService {

    private final ContestRepository contestRepository;
    private final ContestSubmissionItemRepository submissionItemRepository;
    private final ContestSubmissionItemMemoRepository memoRepository;
    private final TeamMemberConvenience teamMemberConvenience;

    public ContestSubmissionItemMemoResponse getMemo(final Long contestId, final Long teamId,
                                                     final Long submissionItemId, final Member member) {
        contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));

        final ContestSubmissionItem submissionItem = submissionItemRepository.findById(submissionItemId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION_ITEM));

        if (!submissionItem.getContest().getId().equals(contestId)) {
            throw new ContestSubmissionItemMemoException(INVALID_SUBMISSION_ITEM_FOR_CONTEST);
        }

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionItemMemo memo = memoRepository.findBySubmissionItemId(submissionItemId)
                .orElseThrow(() -> new ContestSubmissionItemMemoException(NOT_FOUND_MEMO));

        return ContestSubmissionItemMemoResponse.from(memo);
    }
}