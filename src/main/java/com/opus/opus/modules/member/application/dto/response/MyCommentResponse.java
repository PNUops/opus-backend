package com.opus.opus.modules.member.application.dto.response;

import com.opus.opus.modules.team.domain.dao.projection.MyCommentProjection;
import java.time.LocalDateTime;

public record MyCommentResponse(
        CommentInfo comment,
        ProjectInfo project
) {

    public static MyCommentResponse from(final MyCommentProjection result) {
        return new MyCommentResponse(
                new CommentInfo(
                        result.getCommentId(),
                        result.getContent(),
                        result.getCreatedAt(),
                        result.getMemberName()
                ),
                new ProjectInfo(
                        result.getContestId(),
                        result.getContestName(),
                        result.getCategoryName(),
                        result.getTrackName(),
                        result.getTeamId(),
                        result.getTeamName(),
                        result.getProjectName(),
                        result.getOverview()
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
