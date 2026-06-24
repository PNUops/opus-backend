package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.dto.ArchiveFileEntry;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileDocumentQueryService {

    private final FileDocumentRepository fileDocumentRepository;
    private final FileStorage fileStorage;

    public DocumentFileDownload download(final Long fileDocumentId) {
        final FileDocument fileDocument = fileDocumentRepository.findById(fileDocumentId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND));

        final File file = fileDocument.getFile();
        final Resource resource = new ByteArrayResource(fileStorage.load(file.getFilePath()));

        return new DocumentFileDownload(
                resource,
                file.getName(),
                file.getMimeType(),
                file.getFileSize(),
                fileDocument.getSubmissionId()
        );
    }

    public List<ArchiveFileEntry> getArchiveEntries(final List<Long> submissionIds) {
        if (submissionIds.isEmpty()) {
            return List.of();
        }
        return fileDocumentRepository.findAllBySubmissionIdIn(submissionIds).stream()
                .map(fileDocument -> new ArchiveFileEntry(
                        fileDocument.getSubmissionId(),
                        fileDocument.getId(),
                        fileDocument.getFile().getName(),
                        fileDocument.getFile().getFileSize()))
                .toList();
    }

    public InputStream openDocumentStream(final Long fileDocumentId) {
        final FileDocument fileDocument = fileDocumentRepository.findById(fileDocumentId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND));
        return fileStorage.loadAsStream(fileDocument.getFilePath());
    }
}
