package com.opus.opus.modules.team.application.dto.response;

public record TeamCommentResponse(

        Long commentId,
        String description,
        Long memberId,
        String memberName,
        Long teamId
) {
}
