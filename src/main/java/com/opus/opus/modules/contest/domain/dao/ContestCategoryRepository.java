package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestCategoryRepository extends JpaRepository<ContestCategory, Long> {
    boolean existsByCategoryName(String categoryName);
}
