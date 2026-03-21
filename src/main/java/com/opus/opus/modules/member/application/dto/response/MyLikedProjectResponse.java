package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.dao.MyLikedProjectResult;

public record MyLikedProjectResponse(
        Long teamId,
        String teamName,
        String projectName,
        Long contestId,
        String contestName
) {

    public static MyLikedProjectResponse from(final MyLikedProjectResult result) {
        return new MyLikedProjectResponse(
                result.teamId(),
                result.teamName(),
                result.projectName(),
                result.contestId(),
                result.contestName()
        );
    }
}
