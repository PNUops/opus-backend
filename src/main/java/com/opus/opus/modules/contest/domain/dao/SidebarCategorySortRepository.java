package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.SidebarCategorySort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SidebarCategorySortRepository extends JpaRepository<SidebarCategorySort, Long> {

    Optional<SidebarCategorySort> findFirstByOrderByIdAsc();
}
