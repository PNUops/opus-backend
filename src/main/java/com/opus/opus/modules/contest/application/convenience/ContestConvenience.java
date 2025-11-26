package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_UPDATE_TEAM_INFO_FOR_CURRENT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CONTEST_NAME_ALREADY_EXIST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_VOTE_PERIOD_NOW;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestConvenience {

    private final ContestRepository contestRepository;

    public Contest getValidateExistContest(final Long contestId) {
        return contestRepository.findById(contestId).orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));
    }

    public void validateCurrentContest(final Contest contest) {
        if (contest.getIsCurrent()) {
            throw new ContestException(CANNOT_UPDATE_TEAM_INFO_FOR_CURRENT);
        }
    }

    public Contest get6thContest() {
        return contestRepository.findByIsCurrentTrue().orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));
    }

    public void checkDuplicateContestName(String contestName) {
        if (contestRepository.existsByContestName(contestName)) {
            throw new ContestException(CONTEST_NAME_ALREADY_EXIST);
        }
    }

    public void checkVotePeriodNow(Long contestId, LocalDateTime now) {
        Contest contest = getValidateExistContest(contestId);
        if (!(now.isAfter(contest.getVoteStartAt()) && now.isBefore(contest.getVoteEndAt()))) {
            throw new ContestException(NOT_VOTE_PERIOD_NOW);
        }
    }

    @Transactional(propagation = MANDATORY)
    public Contest findByIdForUpdate(final Long contestId) {
        return contestRepository.findByIdForUpdate(contestId).orElseThrow(() -> new ContestException(NOT_FOUND_CONTEST));
    }
}
