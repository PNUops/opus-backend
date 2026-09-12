package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.domain.SidebarSortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_CATEGORY_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_CONTEST_ID_IN_CATEGORY_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_ITEM_ORDER_IN_CATEGORY_CONTEST_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.DUPLICATE_ITEM_ORDER_IN_CATEGORY_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_CONTEST_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_CONTEST_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.INVALID_CATEGORY_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.NOT_EXIST_CATEGORY_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.NOT_EXIST_CONTEST_IN_CATEGORY;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestCategorySortConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.CategoryContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.SidebarCategorySortRequest;
import com.opus.opus.modules.contest.domain.CategoryContestSort;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.SidebarCategorySort;
import com.opus.opus.modules.contest.domain.SidebarSortType;
import com.opus.opus.modules.contest.domain.dao.CategoryContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.exception.ContestCategoryException;
import com.opus.opus.modules.contest.exception.ContestCategoryExceptionType;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCategoryCommandService {

    private final ContestCategoryRepository contestCategoryRepository;
    private final CategoryContestSortRepository categoryContestSortRepository;

    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestCategorySortConvenience contestCategorySortConvenience;

    public void createCategory(final ContestCategoryRequest request) {
        contestCategoryConvenience.validateDuplicateCategoryName(request.categoryName());
        final int itemOrder = (int) contestCategoryRepository.count() + 1;
        final ContestCategory contestCategory = ContestCategory.builder()
                .categoryName(request.categoryName())
                .itemOrder(itemOrder)
                .build();
        contestCategoryRepository.save(contestCategory);

        contestCategorySortConvenience.getOrCreateCategoryContestSort(contestCategory);
    }

    public void updateCategory(final Long categoryId, final ContestCategoryRequest request) {
        contestCategoryConvenience.validateDuplicateCategoryName(request.categoryName());
        final ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(categoryId);
        contestCategory.updateCategory(request.categoryName());
    }

    public void deleteCategory(final Long categoryId) {
        final ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(categoryId);
        contestConvenience.validateAllContestsDeletedInCategory(categoryId);
        categoryContestSortRepository.findByCategoryId(categoryId)
                .ifPresent(categoryContestSortRepository::delete);
        contestCategoryRepository.delete(contestCategory);
    }

    public void updateCategorySort(final SidebarCategorySortRequest request) {
        final SidebarCategorySort sort = contestCategorySortConvenience.getOrCreateSidebarCategorySort();
        sort.updateMode(request.mode());
    }

    public void updateCategorySortCustom(final List<SidebarCategorySortCustomRequest> requests) {
        final SidebarCategorySort sort = contestCategorySortConvenience.getOrCreateSidebarCategorySort();
        checkCustomSort(sort.getMode(), ONLY_CUSTOM_MODE_CAN_CHANGE_CATEGORY_SORT);
        validateDuplicateCategoryIds(requests);
        validateDuplicateCategoryItemOrders(requests);

        final List<ContestCategory> categories = contestCategoryRepository.findAll();
        validateCategoryRequestSizeMatches(requests, categories);
        validateCategoryItemOrderRange(requests, categories.size());

        applyCustomSortToCategories(requests, categories);
    }

    public void updateContestSortInCategory(final Long categoryId, final CategoryContestSortRequest request) {
        final ContestCategory category = contestCategoryConvenience.getValidateExistCategory(categoryId);
        final CategoryContestSort sort = contestCategorySortConvenience.getOrCreateCategoryContestSort(category);
        sort.updateMode(request.mode());
    }

    public void updateContestSortInCategoryCustom(final Long categoryId,
                                                   final List<CategoryContestSortCustomRequest> requests) {
        final ContestCategory category = contestCategoryConvenience.getValidateExistCategory(categoryId);
        final CategoryContestSort sort = contestCategorySortConvenience.getOrCreateCategoryContestSort(category);
        checkCustomSort(sort.getMode(), ONLY_CUSTOM_MODE_CAN_CHANGE_CONTEST_SORT);
        validateDuplicateContestIds(requests);
        validateDuplicateContestItemOrders(requests);

        final List<Contest> contests = contestConvenience.getContestsOfCategory(categoryId);
        validateContestRequestSizeMatches(requests, contests);
        validateContestItemOrderRange(requests, contests.size());

        applyCustomSortToContests(requests, contests);
    }

    private void checkCustomSort(final SidebarSortType mode, final ContestCategoryExceptionType exceptionType) {
        if (mode != CUSTOM) {
            throw new ContestCategoryException(exceptionType);
        }
    }

    private void validateDuplicateCategoryIds(final List<SidebarCategorySortCustomRequest> requests) {
        if (requests.stream().map(SidebarCategorySortCustomRequest::categoryId).distinct().count() != requests.size()) {
            throw new ContestCategoryException(DUPLICATE_CATEGORY_ID_IN_SORT_REQUEST);
        }
    }

    private void validateDuplicateCategoryItemOrders(final List<SidebarCategorySortCustomRequest> requests) {
        if (requests.stream().map(SidebarCategorySortCustomRequest::itemOrder).distinct().count() != requests.size()) {
            throw new ContestCategoryException(DUPLICATE_ITEM_ORDER_IN_CATEGORY_SORT_REQUEST);
        }
    }

    private void validateCategoryRequestSizeMatches(final List<SidebarCategorySortCustomRequest> requests,
                                                     final List<ContestCategory> categories) {
        if (requests.size() != categories.size()) {
            throw new ContestCategoryException(INVALID_CATEGORY_SORT_CUSTOM_REQUEST);
        }
    }

    private void validateCategoryItemOrderRange(final List<SidebarCategorySortCustomRequest> requests,
                                                 final int categoryCount) {
        for (final SidebarCategorySortCustomRequest r : requests) {
            final int order = r.itemOrder();
            if (order < 1 || order > categoryCount) {
                throw new ContestCategoryException(INVALID_CATEGORY_ITEM_ORDER);
            }
        }
    }

    private void applyCustomSortToCategories(final List<SidebarCategorySortCustomRequest> requests,
                                              final List<ContestCategory> categories) {
        final Map<Long, ContestCategory> categoryMap = categories.stream()
                .collect(toMap(ContestCategory::getId, identity()));

        for (final SidebarCategorySortCustomRequest r : requests) {
            final ContestCategory category = categoryMap.get(r.categoryId());
            if (category == null) {
                throw new ContestCategoryException(NOT_EXIST_CATEGORY_IN_SORT_REQUEST);
            }
            category.updateItemOrder(r.itemOrder());
        }
    }

    private void validateDuplicateContestIds(final List<CategoryContestSortCustomRequest> requests) {
        if (requests.stream().map(CategoryContestSortCustomRequest::contestId).distinct().count() != requests.size()) {
            throw new ContestCategoryException(DUPLICATE_CONTEST_ID_IN_CATEGORY_SORT_REQUEST);
        }
    }

    private void validateDuplicateContestItemOrders(final List<CategoryContestSortCustomRequest> requests) {
        if (requests.stream().map(CategoryContestSortCustomRequest::itemOrder).distinct().count() != requests.size()) {
            throw new ContestCategoryException(DUPLICATE_ITEM_ORDER_IN_CATEGORY_CONTEST_SORT_REQUEST);
        }
    }

    private void validateContestRequestSizeMatches(final List<CategoryContestSortCustomRequest> requests,
                                                    final List<Contest> contests) {
        if (requests.size() != contests.size()) {
            throw new ContestCategoryException(INVALID_CATEGORY_CONTEST_SORT_REQUEST);
        }
    }

    private void validateContestItemOrderRange(final List<CategoryContestSortCustomRequest> requests,
                                                final int contestCount) {
        for (final CategoryContestSortCustomRequest r : requests) {
            final int order = r.itemOrder();
            if (order < 1 || order > contestCount) {
                throw new ContestCategoryException(INVALID_CATEGORY_CONTEST_ITEM_ORDER);
            }
        }
    }

    private void applyCustomSortToContests(final List<CategoryContestSortCustomRequest> requests,
                                            final List<Contest> contests) {
        final Map<Long, Contest> contestMap = contests.stream()
                .collect(toMap(Contest::getId, identity()));

        for (final CategoryContestSortCustomRequest r : requests) {
            final Contest contest = contestMap.get(r.contestId());
            if (contest == null) {
                throw new ContestCategoryException(NOT_EXIST_CONTEST_IN_CATEGORY);
            }
            contest.updateItemOrder(r.itemOrder());
        }
    }
}
