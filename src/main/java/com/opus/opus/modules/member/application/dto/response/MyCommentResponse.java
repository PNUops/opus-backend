package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.dao.MyCommentResult;
import java.time.LocalDateTime;

public record MyCommentResponse(
        CommentInfo comment,
        ProjectInfo project
) {

    public static MyCommentResponse from(final MyCommentResult result) {
        return new MyCommentResponse(
                new CommentInfo(
                        result.commentId(),
                        result.content(),
                        result.createdAt(),
                        result.memberName()
                ),
                new ProjectInfo(
                        result.contestId(),
                        result.contestName(),
                        result.categoryName(),
                        result.trackName(),
                        result.teamId(),
                        result.teamName(),
                        result.projectName(),
                        result.overview()
                )
        );
    }

    public record CommentInfo(
            Long commentId,
            String content,
            LocalDateTime createdAt,
            String memberName
    ) {
    }

    public record ProjectInfo(
            Long contestId,
            String contestName,
            String categoryName,
            String trackName,
            Long teamId,
            String teamName,
            String projectName,
            String overview
    ) {
    }
}
