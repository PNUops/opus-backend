package com.opus.opus.modules.contest.domain.dao;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.opus.opus.modules.contest.domain.Contest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    long countByIsCurrentTrue();

    boolean existsByCategoryId(final Long categoryId);

    boolean existsByContestName(final String contestName);

    List<Contest> findAllByIsCurrentTrue();

    @Lock(PESSIMISTIC_WRITE)
    @Query("select c from Contest c where c.id = :contestId")
    Optional<Contest> findByIdForUpdate(final Long contestId);
}
