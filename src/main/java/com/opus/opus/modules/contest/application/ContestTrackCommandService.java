package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestTrackRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
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
    private final TeamConvenience teamConvenience;

    public void createTrack(final Long contestId, final ContestTrackRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());
        final ContestTrack contestTrack = ContestTrack.builder()
                .trackName(request.trackName())
                .contest(contest)
                .build();
        contestTrackRepository.save(contestTrack);
    }

    public void updateTrack(final Long contestId, final Long trackId, final ContestTrackRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());
        final ContestTrack contestTrack = contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        contestTrack.updateTrack(contest, request.trackName());
    }

    public void deleteTrack(final Long contestId, final Long trackId) {
        final ContestTrack contestTrack = contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        teamConvenience.validateAllTeamsDeletedInTrack(trackId);
        contestTrackRepository.delete(contestTrack);
    }
}
