package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.TeamSubmissionStatusResult;
import com.opus.opus.modules.file.domain.FileDocument;
import java.time.LocalDateTime;
import java.util.List;

public record TeamSubmissionItemResponse(

        Long submissionItemId,
        Long submissionId,
        String submissionItemName,
        String description,
        LocalDateTime deadlineAt,
        SubmissionStatus status,
        List<FileResponse> files
) {
    public static TeamSubmissionItemResponse of(final TeamSubmissionStatusResult result,
                                                final List<FileDocument> documents, final SubmissionStatus status) {
        return new TeamSubmissionItemResponse(
                result.submissionItemId(),
                result.submissionId(),
                result.submissionItemName(),
                result.description(),
                result.deadlineAt(),
                status,
                documents.stream().map(FileResponse::from).toList()
        );
    }
}
