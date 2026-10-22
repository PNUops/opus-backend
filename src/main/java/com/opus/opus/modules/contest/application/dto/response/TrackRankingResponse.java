package com.opus.opus.modules.contest.application.dto.response;

import java.util.List;

public record TrackRankingResponse(

        String trackName,
        List<ContestRankingResponse> rankings
) {
}
