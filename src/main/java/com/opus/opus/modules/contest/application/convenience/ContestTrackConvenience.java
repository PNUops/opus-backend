package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.INVALID_TRACK_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.NOT_FOUND_TRACK;
import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.TRACKNAME_DUPLICATED;

import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import java.util.List;
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

    public ContestTrack getValidateExistTrack(final Long contestId, final Long trackId) {
        final ContestTrack contestTrack = contestTrackRepository.findById(trackId)
                .orElseThrow(() -> new ContestTrackException(NOT_FOUND_TRACK));

        if (!contestTrack.getContest().getId().equals(contestId)) {
            throw new ContestTrackException(INVALID_TRACK_FOR_CONTEST);
        }
        return contestTrack;
    }

    public List<ContestTrack> getValidateExistTracks(final Long contestId) {
        return contestTrackRepository.findAllByContestId(contestId);
    }
}
