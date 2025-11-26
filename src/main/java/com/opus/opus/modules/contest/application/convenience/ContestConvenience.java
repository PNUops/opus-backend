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

}
