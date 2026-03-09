package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestAwardConvenience {

    private final ContestAwardRepository contestAwardRepository;

    public List<ContestAward> findAllById(final List<Long> awardIds) {
        final List<ContestAward> contestAwards = contestAwardRepository.findAllById(awardIds);

        if (contestAwards.size() != awardIds.size()) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        return contestAwards;
    }
}
