package com.opus.opus.modules.team.application.dto.response;

public record TeamVoteToggleResponse(
        Long teamId,
        Boolean isVoted,
        String message,
        Long remainingVotesCount,
        Long maxVotesLimit
) {
    public static TeamVoteToggleResponse of(Long teamId, Boolean isVoted, String message,
                                            long currentVoteCount, long maxVotesLimit) {
        long remainingVotesCount = maxVotesLimit - currentVoteCount;
        return new TeamVoteToggleResponse(teamId, isVoted, message, remainingVotesCount, maxVotesLimit);
    }
}
