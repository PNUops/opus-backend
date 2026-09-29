package com.opus.opus.modules.team.application.dto.response;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.TeamCommentVisibility;
import java.time.LocalDateTime;

public record TeamCommentResponse(

        Long commentId,
        String description,
        TeamCommentVisibility visibility,
        Long memberId,
        String memberName,
        String memberRoleType,
        Long teamId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TeamCommentResponse of(final TeamComment comment, final Member member) {
        return new TeamCommentResponse(
                comment.getId(),
                comment.getDescription(),
                comment.getVisibility(),
                comment.getMemberId(),
                member != null ? member.getName() : null,
                member != null ? member.getStaffRoleName() : null,
                comment.getTeam().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
