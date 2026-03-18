package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.dao.MyVoteInfo;

public record MyVoteResponse(
        Long contestId,
        String contestName,
        Long teamId,
        String teamName,
        String projectName
) {
    public static MyVoteResponse from(final MyVoteInfo info) {
        return new MyVoteResponse(
                info.contestId(),
                info.contestName(),
                info.teamId(),
                info.teamName(),
                info.projectName()
        );
    }
}
