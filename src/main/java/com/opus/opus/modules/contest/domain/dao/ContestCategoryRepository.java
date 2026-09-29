package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestCategoryRepository extends JpaRepository<ContestCategory, Long> {

    boolean existsByCategoryName(final String categoryName);

    @Query("""
             SELECT COALESCE(MAX(c.itemOrder), 0)
             FROM ContestCategory c
            """)
    int findMaxItemOrder();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
             UPDATE ContestCategory c
             SET c.itemOrder = c.itemOrder - 1
             WHERE c.itemOrder > :deletedOrder
            """)
    void updateItemOrderAfterDeletion(@Param("deletedOrder") int deletedOrder);
}
