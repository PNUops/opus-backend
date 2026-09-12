package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;

import com.opus.opus.modules.contest.domain.CategoryContestSort;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.SidebarCategorySort;
import com.opus.opus.modules.contest.domain.SidebarSortType;
import com.opus.opus.modules.contest.domain.dao.CategoryContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.SidebarCategorySortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestCategorySortConvenience {

    private final SidebarCategorySortRepository sidebarCategorySortRepository;
    private final CategoryContestSortRepository categoryContestSortRepository;

    @Transactional(readOnly = true)
    public SidebarSortType getCategorySortMode() {
        return sidebarCategorySortRepository.findFirstByOrderByIdAsc()
                .map(SidebarCategorySort::getMode)
                .orElse(ASC);
    }

    @Transactional(readOnly = true)
    public SidebarSortType getContestSortModeInCategory(final Long categoryId) {
        return categoryContestSortRepository.findByCategoryId(categoryId)
                .map(CategoryContestSort::getMode)
                .orElse(ASC);
    }

    @Transactional
    public SidebarCategorySort getOrCreateSidebarCategorySort() {
        return sidebarCategorySortRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> sidebarCategorySortRepository.save(SidebarCategorySort.createDefault()));
    }

    @Transactional
    public CategoryContestSort getOrCreateCategoryContestSort(final ContestCategory category) {
        return categoryContestSortRepository.findByCategoryId(category.getId())
                .orElseGet(() -> categoryContestSortRepository.save(
                        CategoryContestSort.builder().category(category).build()));
    }
}
