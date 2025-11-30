package com.opus.opus.modules.contest.convenience;

import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestAwardConvenience {

    private final ContestAwardRepository contestAwardRepository;

    public boolean existsByContestIdAndAwardName(Long contestId, String awardName) {
        return contestAwardRepository.existsByContestIdAndAwardName(contestId, awardName);
    }

    public void save(ContestAward contestAward) {
        contestAwardRepository.save(contestAward);
    }
}
