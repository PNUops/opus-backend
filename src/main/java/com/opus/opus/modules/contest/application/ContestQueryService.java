package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import java.util.List;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestQueryService {
    public List<ContestCurrentResponse> getCurrentContests()
        return;
    }

    private final ContestRepository contestRepository;
    private final ContestCategoryConvenience contestCategoryConvenience;

    public List<ContestResponse> getAllContests() {
        List<Contest> contests = contestRepository.findAll();

        return contests.stream()
                .map(contest -> {
                    ContestCategory category = contestCategoryConvenience.getValidateExistCategory(
                            contest.getCategoryId());
                    return ContestResponse.from(contest, category.getCategoryName());
                })
                .toList();
    }
}
