package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import com.opus.opus.modules.file.application.dto.CommentFileInfo;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionCommentResponse(

        Long commentId,
        Long memberId,
        String memberName,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String roleType,
        List<ContestSubmissionCommentFileResponse> files
) {
    public static ContestSubmissionCommentResponse of(final ContestSubmissionComment comment, final Member member,
                                                      final List<CommentFileInfo> files) {
        return new ContestSubmissionCommentResponse(
                comment.getId(),
                comment.getMemberId(),
                member != null ? member.getName() : null,
                comment.getDescription(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                extractRoleType(member),
                files.stream()
                        .map(file -> new ContestSubmissionCommentFileResponse(
                                file.fileId(), file.fileName(), file.fileSize()))
                        .toList()
        );
    }

    private static String extractRoleType(final Member member) {
        if (member == null) {
            return null;
        }
        return member.getRoles().stream()
                .findFirst()
                .map(MemberRoleType::name)
                .orElse(null);
    }
}
