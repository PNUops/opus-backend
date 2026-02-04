package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.dto.response.ContestTrackResponse;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestTrackQueryService {

    private final ContestTrackRepository contestTrackRepository;

    public List<ContestTrackResponse> getAllContestTracks(final Long contestId) {
        final List<ContestTrack> contestTracks = contestTrackRepository.findAllByContestId(contestId);
        return contestTracks.stream()
                .map(ContestTrackResponse::from)
                .toList();
    }
}
