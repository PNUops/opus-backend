package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.MEMO_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.NOT_FOUND_MEMO;

import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemMemoRequest;
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
@Transactional
public class ContestSubmissionItemMemoCommandService {

    private final ContestRepository contestRepository;
    private final ContestSubmissionItemRepository submissionItemRepository;
    private final ContestSubmissionItemMemoRepository memoRepository;
    private final TeamMemberConvenience teamMemberConvenience;

    public void createMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final ContestSubmissionItemMemoRequest request, final Member member) {
        final ContestSubmissionItem submissionItem = getValidatedSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        if (memoRepository.existsBySubmissionItemId(submissionItemId)) {
            throw new ContestSubmissionItemMemoException(MEMO_ALREADY_EXISTS);
        }

        memoRepository.save(ContestSubmissionItemMemo.builder()
                .content(request.content())
                .submissionItem(submissionItem)
                .build());
    }

    public void updateMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final ContestSubmissionItemMemoRequest request, final Member member) {
        getValidatedSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionItemMemo memo = memoRepository.findBySubmissionItemId(submissionItemId)
                .orElseThrow(() -> new ContestSubmissionItemMemoException(NOT_FOUND_MEMO));

        memo.updateContent(request.content());
    }

    public void deleteMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final Member member) {
        getValidatedSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionItemMemo memo = memoRepository.findBySubmissionItemId(submissionItemId)
                .orElseThrow(() -> new ContestSubmissionItemMemoException(NOT_FOUND_MEMO));

        memoRepository.delete(memo);
    }

    private ContestSubmissionItem getValidatedSubmissionItem(final Long contestId, final Long submissionItemId) {
        contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));

        final ContestSubmissionItem submissionItem = submissionItemRepository.findById(submissionItemId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_SUBMISSION_ITEM));

        if (!submissionItem.getContest().getId().equals(contestId)) {
            throw new ContestSubmissionItemMemoException(INVALID_SUBMISSION_ITEM_FOR_CONTEST);
        }

        return submissionItem;
    }
}