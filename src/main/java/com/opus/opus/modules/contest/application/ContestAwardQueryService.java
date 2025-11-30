package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;

import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.dto.response.ContestAwardResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestAwardQueryService {

    private final ContestRepository contestRepository;
    private final ContestAwardRepository contestAwardRepository;

    public List<ContestAwardResponse> getContestAwards(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new ContestException(NOT_FOUND_CONTEST);
        }

        return contestAwardRepository.findByContestId(contestId).stream()
                .map(ContestAwardResponse::new)
                .collect(Collectors.toList());
    }
}
