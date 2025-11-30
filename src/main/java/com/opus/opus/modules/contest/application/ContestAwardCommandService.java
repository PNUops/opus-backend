package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.DUPLICATE_CONTEST_AWARD_NAME;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;

import com.opus.opus.modules.contest.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.dto.request.ContestAwardRequest;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import com.opus.opus.modules.contest.exception.ContestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestAwardCommandService {

    private final ContestRepository contestRepository;
    private final ContestAwardConvenience contestAwardConvenience;

    public void createContestAward(Long contestId, ContestAwardRequest request) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));

        if (contestAwardConvenience.existsByContestIdAndAwardName(contestId, request.getAwardName())) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }

        ContestAward contestAward = ContestAward.builder()
                .contest(contest)
                .awardName(request.getAwardName())
                .awardColor(request.getAwardColor())
                .build();

        contestAwardConvenience.save(contestAward);
    }
}
