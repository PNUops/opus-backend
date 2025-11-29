package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestCategoryRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCategoryCommandService {

    private final ContestCategoryRepository contestCategoryRepository;
    private final ContestCategoryConvenience contestCategoryConvenience;

    private final ContestConvenience contestConvenience;

    public ContestCategoryResponse createCategory(final ContestCategoryRequest request) {
        contestCategoryConvenience.validateDuplicateCategoryName(request.categoryName());
        final ContestCategory contestCategory = ContestCategory.builder()
                .categoryName(request.categoryName())
                .build();
        contestCategoryRepository.save(contestCategory);

        return ContestCategoryResponse.from(contestCategory);
    }

    public void updateCategory(final Long categoryId, final ContestCategoryRequest request) {
        contestCategoryConvenience.validateDuplicateCategoryName(request.categoryName());
        final ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(categoryId);
        contestCategory.updateCategory(request.categoryName());
    }

    public void deleteCategory(final Long categoryId) {
        final ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(categoryId);
        contestConvenience.validateAllContestsDeleted(categoryId);
        contestCategoryRepository.delete(contestCategory);
    }
}
