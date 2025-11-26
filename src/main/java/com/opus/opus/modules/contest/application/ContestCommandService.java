package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
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
    private final TeamConvenience teamConvenience;

    public void createContest(final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = Contest.builder()
                .contestName(request.contestName())
                .categoryId(request.categoryId())
                .build();
        contestRepository.save(contest);
    }

    public void updateContest(final Long contestId, final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contest.updateContest(request.categoryId(), request.contestName());
    }

    public void deleteContest(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        teamConvenience.validateAllTeamsDeleted(contestId);
        contestRepository.delete(contest);
    }
}
