package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestCategoryQueryService {
    private final ContestCategoryRepository contestCategoryRepository;

    public List<ContestCategoryResponse> getAllContestCategories() {
        List<ContestCategory> ContestCategories = contestCategoryRepository.findAll();

        return ContestCategories.stream()
                .map(ContestCategoryResponse::from)
                .toList();
    }
}
