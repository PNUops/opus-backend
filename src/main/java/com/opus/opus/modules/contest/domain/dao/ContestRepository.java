package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.Contest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    long countByIsCurrentTrue();

    boolean existsByCategoryId(final Long categoryId);

    boolean existsByContestName(final String contestName);

    List<Contest> findAllByIsCurrentTrue();
}
