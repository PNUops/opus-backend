package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.DUPLICATE_CONTEST_AWARD_NAME;
import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.dto.request.ContestAwardRequest;
import com.opus.opus.modules.contest.exception.ContestAwardException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestAwardCommandService {

    private final ContestConvenience contestConvenience;
    private final ContestAwardConvenience contestAwardConvenience;

    public void createContestAward(Long contestId, ContestAwardRequest request) {
        Contest contest = contestConvenience.getContestById(contestId);

        if (contestAwardConvenience.isDuplicateAwardName(contestId, request.getAwardName())) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }

        ContestAward contestAward = ContestAward.builder()
                .contest(contest)
                .awardName(request.getAwardName())
                .awardColor(request.getAwardColor())
                .build();

        contestAwardConvenience.save(contestAward);
    }

    public void updateContestAward(Long contestId, Long awardId, ContestAwardRequest request) {
        contestConvenience.getContestById(contestId);

        ContestAward contestAward = contestAwardConvenience.getContestAwardById(awardId);

        if (!contestAward.getContest().getId().equals(contestId)) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        if (!contestAward.getAwardName().equals(request.getAwardName()) && contestAwardConvenience.isDuplicateAwardName(contestId, request.getAwardName())) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }

        contestAward.update(request.getAwardName(), request.getAwardColor());
    }
}
