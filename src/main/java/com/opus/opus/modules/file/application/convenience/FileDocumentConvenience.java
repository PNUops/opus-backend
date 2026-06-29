package com.opus.opus.modules.file.application.convenience;

import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileDocumentConvenience {

    private final FileDocumentRepository fileDocumentRepository;

    public List<FileDocument> findAllBySubmissionId(final Long submissionId) {
        return fileDocumentRepository.findAllBySubmissionIdOrderByFileOrder(submissionId);
    }

    public List<FileDocument> findAllBySubmissionIds(final List<Long> submissionIds) {
        return fileDocumentRepository.findAllBySubmissionIdInOrderBySubmissionIdAscFileOrderAsc(submissionIds);
    }

    public void validateFileBelongsToSubmission(final Long submissionId, final Long fileId) {
        if (!fileDocumentRepository.existsByIdAndSubmissionId(fileId, submissionId)) {
            throw new FileException(FileExceptionType.NOT_FOUND);
        }
    }

    public long countBySubmissionId(final Long submissionId) {
        return fileDocumentRepository.countBySubmissionId(submissionId);
    }
}
