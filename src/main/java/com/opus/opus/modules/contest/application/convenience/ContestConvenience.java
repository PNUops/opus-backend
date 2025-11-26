package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.CATEGORY_HAS_CONTEST;

import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestConvenience {
    private final ContestRepository contestCRepository;

    public void validateAllContestsDeleted(final Long categoryId) {
        if (contestCRepository.existsByCategoryId(categoryId)) {
            throw new ContestException(CATEGORY_HAS_CONTEST);
        }
    }
}
