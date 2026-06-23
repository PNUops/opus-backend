package com.opus.opus.modules.file.application.convenience;

import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
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
}
