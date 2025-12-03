package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.DUPLICATE_CONTEST_AWARD_NAME;
import static com.opus.opus.modules.contest.exception.ContestAwardExceptionType.NOT_FOUND_CONTEST_AWARD;

import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.application.dto.request.ContestAwardRequest;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
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
    private final ContestAwardRepository contestAwardRepository;

    public void createContestAward(final Long contestId, final ContestAwardRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        validateDuplicateAwardName(contestId, request.awardName());

        final ContestAward contestAward = ContestAward.builder()
                .contest(contest)
                .awardName(request.awardName())
                .awardColor(request.awardColor())
                .build();

        contestAwardRepository.save(contestAward);
    }

    public void updateContestAward(final Long contestId, final Long awardId, final ContestAwardRequest request) {
        contestConvenience.getValidateExistContest(contestId);

        final ContestAward contestAward = contestAwardConvenience.getValidateContestAward(awardId);

        validateContestAwardBelonging(contestAward, contestId);
        validateDuplicateAwardNameForUpdate(contestAward, contestId, request.awardName());

        contestAward.update(request.awardName(), request.awardColor());
    }

    public void deleteContestAward(final Long contestId, final Long awardId) {
        contestConvenience.getValidateExistContest(contestId);

        final ContestAward contestAward = contestAwardConvenience.getValidateContestAward(awardId);

        validateContestAwardBelonging(contestAward, contestId);

        contestAwardRepository.delete(contestAward);
    }

    private void validateDuplicateAwardName(final Long contestId, final String awardName) {
        if (contestAwardRepository.existsByContestIdAndAwardName(contestId, awardName)) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }
    }

    private void validateContestAwardBelonging(final ContestAward contestAward, final Long contestId) {
        if (!contestAward.getContest().getId().equals(contestId)) {
            throw new ContestAwardException(NOT_FOUND_CONTEST_AWARD);
        }
    }

    private void validateDuplicateAwardNameForUpdate(final ContestAward contestAward, final Long contestId, final String newAwardName) {
        if (!contestAward.getAwardName().equals(newAwardName)
                && contestAwardRepository.existsByContestIdAndAwardName(contestId, newAwardName)) {
            throw new ContestAwardException(DUPLICATE_CONTEST_AWARD_NAME);
        }
    }
}
