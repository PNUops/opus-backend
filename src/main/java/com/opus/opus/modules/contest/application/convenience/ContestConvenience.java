package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.CATEGORY_HAS_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CONTEST_NAME_ALREADY_EXIST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestConvenience {
    private final ContestRepository contestRepository;

    public void validateAllContestsDeleted(final Long categoryId) {
        if (contestRepository.existsByCategoryId(categoryId)) {
            throw new ContestException(CATEGORY_HAS_CONTEST);
        }
    }

    public void validateDuplicateContestName(final String contestName) {
        if (contestRepository.existsByContestName(contestName)) {
            throw new ContestException(CONTEST_NAME_ALREADY_EXIST);
        }
    }

    public Contest getValidateExistContest(final Long contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));
    }
}
