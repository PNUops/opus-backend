package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.dto.response.ContestCategoryResponse;
import com.opus.opus.modules.contest.application.dto.response.SidebarResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
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

    public List<ContestCategoryResponse> getAllContestCategories() {
        List<ContestCategory> ContestCategories = contestCategoryRepository.findAll();

        return ContestCategories.stream()
                .map(ContestCategoryResponse::from)
                .toList();
    }

    public List<SidebarResponse> getSidebar() {
        final List<ContestCategory> categories = contestCategoryRepository.findAll();
        final Map<Long, List<Contest>> contestsByCategory = contestRepository.findAll().stream()
                .sorted(Comparator.comparing(Contest::getCreatedAt))
                .collect(Collectors.groupingBy(Contest::getCategoryId));

        return categories.stream()
                .sorted(Comparator.comparing(ContestCategory::getCreatedAt))
                .map(category -> SidebarResponse.of(category, contestsByCategory.getOrDefault(category.getId(), List.of())))
                .toList();
    }
}
