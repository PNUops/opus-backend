package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.application.dto.response.ContestAwardResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestAwardQueryService {

    private final ContestConvenience contestConvenience;

    private final ContestAwardRepository contestAwardRepository;

    public List<ContestAwardResponse> getContestAwards(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        return contestAwardRepository.findByContestId(contestId).stream()
                .map(ContestAwardResponse::from)
                .toList();
    }
}
