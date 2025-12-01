package com.opus.opus.modules.contest.convenience;

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

    public ContestAward getContestAwardById(Long awardId) {
        return contestAwardRepository.findById(awardId)
                .orElseThrow(() -> new ContestAwardException(NOT_FOUND_CONTEST_AWARD));
    }

    public List<ContestAward> findAllById(List<Long> awardIds) {
        return contestAwardRepository.findAllById(awardIds);
    }

    public boolean isDuplicateAwardName(Long contestId, String awardName) {
        return contestAwardRepository.existsByContestIdAndAwardName(contestId, awardName);
    }

    public void save(ContestAward contestAward) {
        contestAwardRepository.save(contestAward);
    }

    public void delete(ContestAward contestAward) {
        contestAwardRepository.delete(contestAward);
    }
}
