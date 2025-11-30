package com.opus.opus.modules.contest.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestConvenience {

    private final ContestRepository contestRepository;

    public Contest getContestById(Long contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));
    }
}
