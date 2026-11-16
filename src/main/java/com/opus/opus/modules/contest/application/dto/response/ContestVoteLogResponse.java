package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamVote;
import java.time.LocalDateTime;

public record ContestVoteLogResponse(

        String voterName,
        String voterEmail,
        String teamName,
        LocalDateTime votedAt
) {
    public static ContestVoteLogResponse of(final TeamVote vote, final Member member) {
        return new ContestVoteLogResponse(
                member != null ? member.getName() : null,
                member != null ? member.getEmail() : null,
                vote.getTeam().getTeamName(),
                vote.getCreatedAt()
        );
    }
}
