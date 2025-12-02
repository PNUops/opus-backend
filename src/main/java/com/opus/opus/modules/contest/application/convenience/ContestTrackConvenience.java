package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.TRACKNAME_DUPLICATED;

import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestTrackConvenience {
    private final ContestTrackRepository contestTrackRepository;

    public void validateDuplicateTrackName(final Long contestId, final String trackName) {
        if (contestTrackRepository.existsByContestIdAndTrackName(contestId, trackName)) {
            throw new ContestTrackException(TRACKNAME_DUPLICATED);
        }
    }
}
