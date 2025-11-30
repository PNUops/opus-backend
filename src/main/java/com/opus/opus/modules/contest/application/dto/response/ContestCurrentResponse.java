package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.Contest;
import java.time.LocalDateTime;

public record ContestCurrentResponse(
        Long contestId,
        String categoryName,
        String contestName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        Long bannerId
) {
    public static ContestCurrentResponse of(
            Contest contest,
            String categoryName
    ) {
        return new ContestCurrentResponse(
                contest.getId(),
                categoryName,
                contest.getContestName(),
                contest.getVoteStartAt(),
                contest.getVoteEndAt(),
                contest.getBannerId()
        );
    }
}
