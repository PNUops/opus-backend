package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionStatus;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.team.domain.Team;
import java.time.LocalDateTime;
import java.util.List;

public record ContestSubmissionDetailResponse(
        Long submissionId,
        Long teamId,
        String teamName,
        String projectOverview,
        String trackName,
        String submissionTypeName,
        SubmissionStatus status,
        LocalDateTime deadlineAt,
        LocalDateTime firstSubmittedAt,
        LocalDateTime lastModifiedAt,
        List<ContestSubmissionFileResponse> files,
        Integer commentCount
) {
    public static ContestSubmissionDetailResponse of(final ContestSubmission submission, final Team team,
                                                     final String trackName, final List<FileDocument> fileDocuments,
                                                     final int commentCount) {
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();
        return new ContestSubmissionDetailResponse(
                submission.getId(),
                team.getId(),
                team.getTeamName(),
                team.getOverview(),
                trackName,
                submissionItem.getName(),
                submission.isLate() ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED,
                submissionItem.getEndAt(),
                submission.getFirstSubmittedAt(),
                submission.getUpdatedAt(),
                fileDocuments.stream().map(ContestSubmissionFileResponse::from).toList(),
                commentCount
        );
    }
}
