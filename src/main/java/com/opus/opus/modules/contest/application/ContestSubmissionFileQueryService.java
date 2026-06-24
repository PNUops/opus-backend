package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionFileQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public DocumentFileDownload downloadSubmissionFile(final Long contestId, final Long submissionId,
                                                       final Long fileId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final DocumentFileDownload download = fileDocumentQueryService.download(fileId);
        if (!download.submissionId().equals(submissionId)) {
            throw new FileException(FileExceptionType.NOT_FOUND);
        }
        return download;
    }
}
