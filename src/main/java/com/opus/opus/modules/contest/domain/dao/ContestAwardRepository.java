package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestAward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestAwardRepository extends JpaRepository<ContestAward, Long> {
    boolean existsByContestIdAndAwardName(Long contestId, String awardName);
}
