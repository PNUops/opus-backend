package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST_SORT;

import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.dao.ContestSortRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSortConvenience {

    private final ContestSortRepository contestSortRepository;

    public ContestSort getValidateExistContestSort(final Long contestId) {
        return contestSortRepository.findByContestId(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST_SORT));
    }
}
