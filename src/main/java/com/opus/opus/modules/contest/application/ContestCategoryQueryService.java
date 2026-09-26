package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestCategorySortConvenience;
import com.opus.opus.modules.contest.application.dto.response.CategoryContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarCategorySortResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import com.opus.opus.modules.contest.domain.CategoryContestSort;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.SidebarSortType;
import com.opus.opus.modules.contest.domain.dao.CategoryContestSortRepository;
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
    private final CategoryContestSortRepository categoryContestSortRepository;
    private final ContestCategorySortConvenience contestCategorySortConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;

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
        contestCategoryConvenience.getValidateExistCategory(categoryId);
        final SidebarSortType mode = contestCategorySortConvenience.getContestSortModeInCategory(categoryId);
        return new CategoryContestSortResponse(mode);
    }

    public List<SidebarResponse> getSidebar() {
        final List<ContestCategory> categories = sortCategories(contestCategoryRepository.findAll());
        final Map<Long, List<Contest>> contestsByCategory = contestRepository.findAll().stream()
                .collect(Collectors.groupingBy(Contest::getCategoryId));
        final Map<Long, SidebarSortType> contestSortModeByCategory = categoryContestSortRepository.findAll().stream()
                .collect(Collectors.toMap(sort -> sort.getCategory().getId(), CategoryContestSort::getMode));

        return categories.stream()
                .map(category -> SidebarResponse.of(category,
                        sortContestsInCategory(contestsByCategory.getOrDefault(category.getId(), List.of()),
                                contestSortModeByCategory.getOrDefault(category.getId(), ASC))))
                .toList();
    }

    private List<ContestCategory> sortCategories(final List<ContestCategory> categories) {
        final SidebarSortType mode = contestCategorySortConvenience.getCategorySortMode();
        final Comparator<ContestCategory> comparator = switch (mode) {
            case ASC -> Comparator.comparing(ContestCategory::getCategoryName);
            case DESC -> Comparator.comparing(ContestCategory::getCategoryName).reversed();
            case CUSTOM -> Comparator.comparing(ContestCategory::getItemOrder).thenComparing(ContestCategory::getId);
        };
        return categories.stream().sorted(comparator).toList();
    }

    private List<Contest> sortContestsInCategory(final List<Contest> contests, final SidebarSortType mode) {
        final Comparator<Contest> comparator = switch (mode) {
            case ASC -> Comparator.comparing(Contest::getContestName);
            case DESC -> Comparator.comparing(Contest::getContestName).reversed();
            case CUSTOM -> Comparator.comparing(Contest::getItemOrder).thenComparing(Contest::getId);
        };
        return contests.stream().sorted(comparator).toList();
    }
}
