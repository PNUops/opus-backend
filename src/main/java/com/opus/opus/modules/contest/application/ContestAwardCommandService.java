package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.DUPLICATE_CONTEST_AWARD_NAME;
import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.application.dto.request.ContestAwardRequest;
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

    public void createContestAward(final Long contestId, final ContestAwardRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        if (contestAwardConvenience.isDuplicateAwardName(contestId, request.awardName())) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }

        final ContestAward contestAward = ContestAward.builder()
                .contest(contest)
                .awardName(request.awardName())
                .awardColor(request.awardColor())
                .build();

        contestAwardConvenience.save(contestAward);
    }

    public void updateContestAward(final Long contestId, final Long awardId, final ContestAwardRequest request) {
        contestConvenience.getValidateExistContest(contestId);

        final ContestAward contestAward = contestAwardConvenience.getContestAwardById(awardId);

        if (!contestAward.getContest().getId().equals(contestId)) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        if (!contestAward.getAwardName().equals(request.awardName()) && contestAwardConvenience.isDuplicateAwardName(contestId, request.awardName())) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }

        contestAward.update(request.awardName(), request.awardColor());
    }

    public void deleteContestAward(final Long contestId, final Long awardId) {
        contestConvenience.getValidateExistContest(contestId);

        final ContestAward contestAward = contestAwardConvenience.getContestAwardById(awardId);
        if (!contestAward.getContest().getId().equals(contestId)) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }

        contestAwardConvenience.delete(contestAward);
    }
}
