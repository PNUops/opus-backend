package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestTrackRequest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestTrackCommandService {
    private final ContestTrackRepository contestTrackRepository;

    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestConvenience contestConvenience;

    public void createTrack(final Long contestId, final ContestTrackRequest request) {
        contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());
        final ContestTrack contestTrack = ContestTrack.builder()
                .trackName(request.trackName())
                .contestId(contestId)
                .build();
        contestTrackRepository.save(contestTrack);
    }

    public void updateTrack(final Long contestId, final Long trackId, final ContestTrackRequest request) {
        contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());

        final ContestTrack contestTrack = contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        contestTrack.updateTrack(contestId, request.trackName());
    }
}
