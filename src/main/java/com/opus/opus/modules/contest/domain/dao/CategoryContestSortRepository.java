package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.CategoryContestSort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryContestSortRepository extends JpaRepository<CategoryContestSort, Long> {

    Optional<CategoryContestSort> findByCategoryId(final Long categoryId);
}
