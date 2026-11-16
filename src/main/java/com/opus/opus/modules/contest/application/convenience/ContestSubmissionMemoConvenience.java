package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;

import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionMemoRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionMemoConvenience {

    private final ContestSubmissionMemoRepository contestSubmissionMemoRepository;

    public ContestSubmissionMemo getValidateExistMemo(final Long submissionItemId, final Long teamId) {
        return contestSubmissionMemoRepository.findBySubmissionItemIdAndTeamId(submissionItemId, teamId)
                .orElseThrow(() -> new ContestSubmissionMemoException(NOT_FOUND_MEMO));
    }

    public boolean isMemoWritten(final Long submissionItemId, final Long teamId) {
        return contestSubmissionMemoRepository.existsBySubmissionItemIdAndTeamId(submissionItemId, teamId);
    }
}
