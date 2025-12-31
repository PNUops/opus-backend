package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.CATEGORY_NAME_ALREADY_EXIST;
import static com.opus.opus.modules.contest.exception.ContestCategoryExceptionType.NOT_FOUND_CATEGORY;

import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.exception.ContestCategoryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestCategoryConvenience {

    private final ContestCategoryRepository contestCategoryRepository;

    public ContestCategory getValidateExistCategory(final Long categoryId) {
        return contestCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ContestCategoryException(NOT_FOUND_CATEGORY));
    }

    public void validateDuplicateCategoryName(final String categoryName) {
        if (contestCategoryRepository.existsByCategoryName(categoryName)) {
            throw new ContestCategoryException(CATEGORY_NAME_ALREADY_EXIST);
        }
    }
}
