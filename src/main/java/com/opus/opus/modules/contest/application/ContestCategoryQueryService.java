package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestCategorySortConvenience;
import com.opus.opus.modules.contest.application.dto.response.CategoryContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarCategorySortResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.SidebarSortType;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestCategoryQueryService {

    private final ContestCategoryRepository contestCategoryRepository;
    private final ContestRepository contestRepository;
    private final ContestCategorySortConvenience contestCategorySortConvenience;

    public List<ContestCategoryResponse> getAllContestCategories() {
        List<ContestCategory> ContestCategories = contestCategoryRepository.findAll();

        return ContestCategories.stream()
                .map(ContestCategoryResponse::from)
                .toList();
    }

    public SidebarCategorySortResponse getCategorySort() {
        final SidebarSortType mode = contestCategorySortConvenience.getCategorySortMode();
        return new SidebarCategorySortResponse(mode);
    }

    public CategoryContestSortResponse getContestSortInCategory(final Long categoryId) {
        final SidebarSortType mode = contestCategorySortConvenience.getContestSortModeInCategory(categoryId);
        return new CategoryContestSortResponse(mode);
    }

    public List<SidebarResponse> getSidebar() {
        final List<ContestCategory> categories = sortCategories(contestCategoryRepository.findAll());
        final Map<Long, List<Contest>> contestsByCategory = contestRepository.findAll().stream()
                .collect(Collectors.groupingBy(Contest::getCategoryId));

        return categories.stream()
                .map(category -> SidebarResponse.of(category,
                        sortContestsInCategory(category, contestsByCategory.getOrDefault(category.getId(), List.of()))))
                .toList();
    }

    private List<ContestCategory> sortCategories(final List<ContestCategory> categories) {
        final SidebarSortType mode = contestCategorySortConvenience.getCategorySortMode();
        final Comparator<ContestCategory> comparator = switch (mode) {
            case ASC -> Comparator.comparing(ContestCategory::getCategoryName);
            case DESC -> Comparator.comparing(ContestCategory::getCategoryName).reversed();
            case CUSTOM -> Comparator.comparing(ContestCategory::getItemOrder);
        };
        return categories.stream().sorted(comparator).toList();
    }

    private List<Contest> sortContestsInCategory(final ContestCategory category, final List<Contest> contests) {
        final SidebarSortType mode = contestCategorySortConvenience.getContestSortModeInCategory(category.getId());
        final Comparator<Contest> comparator = switch (mode) {
            case ASC -> Comparator.comparing(Contest::getContestName);
            case DESC -> Comparator.comparing(Contest::getContestName).reversed();
            case CUSTOM -> Comparator.comparing(Contest::getItemOrder);
        };
        return contests.stream().sorted(comparator).toList();
    }
}
