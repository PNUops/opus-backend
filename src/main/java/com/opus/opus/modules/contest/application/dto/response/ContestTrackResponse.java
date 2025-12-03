package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestTrack;
import java.time.LocalDateTime;

public record ContestTrackResponse(
        Long trackId,
        String trackName,
        LocalDateTime updatedAt

) {
    public static ContestTrackResponse from(ContestTrack contestTrack) {
        return new ContestTrackResponse(
                contestTrack.getId(),
                contestTrack.getTrackName(),
                contestTrack.getUpdatedAt()
        );
    }
}
