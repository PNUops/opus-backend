package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.file.domain.dao.SubmissionFileInfo;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record TeamSubmissionsResponse(
        Long teamId,
        String teamName,
        String projectName,
        String trackName,
        long pendingFeedbackCount,
        List<SubmissionResponse> submissions
) {
    public static TeamSubmissionsResponse of(final Team team, final String trackName, final List<SubmissionResponse> submissions) {
        final long pendingFeedbackCount = submissions.stream()
                .filter(submission -> submission.feedbackStatus() == FeedbackStatus.PENDING)
                .count();
        return new TeamSubmissionsResponse(
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                trackName,
                pendingFeedbackCount,
                submissions
        );
    }

    public record SubmissionResponse(
            Long submissionId,
            Long submissionItemId,
            String submissionItemName,
            FeedbackStatus feedbackStatus,
            List<FileResponse> files
    ) {
        public static SubmissionResponse of(final ContestSubmission submission, final boolean reviewed,
                                            final List<FileResponse> files) {
            return new SubmissionResponse(
                    submission.getId(),
                    submission.getSubmissionItem().getId(),
                    submission.getSubmissionItem().getName(),
                    reviewed ? FeedbackStatus.COMPLETED : FeedbackStatus.PENDING,
                    files
            );
        }
    }

    public record FileResponse(
            Long fileId,
            String fileName,
            Long fileSize
    ) {
        public static FileResponse from(final SubmissionFileInfo fileInfo) {
            return new FileResponse(fileInfo.fileDocumentId(), fileInfo.fileName(), fileInfo.fileSize());
        }
    }

    public enum FeedbackStatus {
        COMPLETED,
        PENDING
    }
}
