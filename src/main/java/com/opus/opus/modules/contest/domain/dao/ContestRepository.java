package com.opus.opus.modules.contest.domain.dao;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.opus.opus.modules.contest.domain.Contest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    long countByIsCurrentTrue();

    boolean existsByCategoryId(final Long categoryId);

    boolean existsByContestName(final String contestName);

    List<Contest> findAllByCategoryId(final Long categoryId);

    List<Contest> findAllByIsCurrentTrue();

    @Lock(PESSIMISTIC_WRITE)
    @Query("select c from Contest c where c.id = :contestId")
    Optional<Contest> findByIdForUpdate(final Long contestId);

    @Query("""
             SELECT COALESCE(MAX(c.itemOrder), 0)
             FROM Contest c
             WHERE c.categoryId = :categoryId
            """)
    int findMaxItemOrderByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
             UPDATE Contest c
             SET c.itemOrder = c.itemOrder - 1
             WHERE c.categoryId = :categoryId
               AND c.itemOrder > :deletedOrder
            """)
    void updateItemOrderAfterDeletion(@Param("categoryId") Long categoryId, @Param("deletedOrder") int deletedOrder);
}
