package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestAward;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestAwardRepository extends JpaRepository<ContestAward, Long> {

    boolean existsByContestIdAndAwardName(final Long contestId, final String awardName);

    List<ContestAward> findByContestId(final Long contestId);

    Optional<ContestAward> findByIdAndContestId(Long id, Long contestId);
}
