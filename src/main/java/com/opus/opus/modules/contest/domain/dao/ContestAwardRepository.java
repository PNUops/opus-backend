package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestAward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestAwardRepository extends JpaRepository<ContestAward, Long> {

    boolean existsByContestIdAndAwardName(Long contestId, String awardName);

    List<ContestAward> findByContestId(Long contestId);
}
