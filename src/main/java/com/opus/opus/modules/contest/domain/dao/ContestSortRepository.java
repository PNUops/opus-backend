package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSortRepository extends JpaRepository<ContestSort, Long> {

    Optional<ContestSort> findByContestId(final Long contestId);
}
