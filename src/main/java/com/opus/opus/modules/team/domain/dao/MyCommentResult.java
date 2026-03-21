package com.opus.opus.modules.team.domain.dao;

import java.time.LocalDateTime;

public record MyCommentResult(
        Long commentId,
        String content,
        LocalDateTime createdAt,
        String memberName,
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
