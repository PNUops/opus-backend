package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.dao.MyLikedProjectResult;

public record MyLikePreviewResponse(
        Long teamId,
        String teamName,
        Long contestId,
        String projectName,
        String contestName
) {

    public static MyLikePreviewResponse from(final MyLikedProjectResult result) {
        return new MyLikePreviewResponse(
                result.teamId(),
                result.teamName(),
                result.contestId(),
                result.projectName(),
                result.contestName()
        );
    }
}
