package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCommandService {

    private final ContestRepository contestRepository;
    private final ContestConvenience contestConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;

    public void createContest(final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = Contest.builder()
                .contestName(request.contestName())
                .categoryId(request.categoryId())
                .build();
        contestRepository.save(contest);
    }
}
