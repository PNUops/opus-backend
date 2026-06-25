package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class FileDocumentCommandService {

    private final FileDocumentRepository fileDocumentRepository;
    private final FilePathGenerator filePathGenerator;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher eventPublisher;

    public FileDocument storeDocumentFile(final MultipartFile multipartFile,
                                          final Long submissionId, final Integer fileOrder) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException(FileExceptionType.EMPTY_FILE);
        }

        try {
            final byte[] fileBytes = multipartFile.getBytes();
            final String extension = extractExtension(multipartFile.getOriginalFilename());
            final String relativePath = filePathGenerator.generate(extension);
            final String mimeType = multipartFile.getContentType() != null
                    ? multipartFile.getContentType() : "application/octet-stream";

            final File file = File.create(multipartFile.getOriginalFilename(), relativePath, mimeType, multipartFile.getSize());

            final FileDocument fileDocument = FileDocument.builder()
                    .file(file)
                    .submissionId(submissionId)
                    .fileOrder(fileOrder)
                    .build();

            final FileDocument savedDocument = fileDocumentRepository.save(fileDocument);

            fileStorage.store(fileBytes, relativePath);

            return savedDocument;
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    public void storeDocumentFiles(final Long submissionId, final List<MultipartFile> files) {
        final int startOrder = fileDocumentRepository.findAllBySubmissionIdOrderByFileOrder(submissionId)
                .stream()
                .mapToInt(FileDocument::getFileOrder)
                .max()
                .orElse(0);
        for (int i = 0; i < files.size(); i++) {
            storeDocumentFile(files.get(i), submissionId, startOrder + 1 + i);
        }
    }

    public void deleteDocumentFile(final Long fileDocumentId) {
        final FileDocument fileDocument = fileDocumentRepository.findById(fileDocumentId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND,
                        "삭제할 파일을 찾을 수 없습니다. ID=" + fileDocumentId));

        final String filePath = fileDocument.getFilePath();
        fileDocumentRepository.delete(fileDocument);
        eventPublisher.publishEvent(new PhysicalFileDeleteEvent(filePath));
    }

    public void deleteAllBySubmissionId(final Long submissionId) {
        final List<FileDocument> documents = fileDocumentRepository
                .findAllBySubmissionIdOrderByFileOrder(submissionId);
        documents.forEach(doc -> {
            final String filePath = doc.getFilePath();
            fileDocumentRepository.delete(doc);
            eventPublisher.publishEvent(new PhysicalFileDeleteEvent(filePath));
        });
    }

    private String extractExtension(final String filename) {
        if (filename == null || filename.isBlank()) {
            return "bin";
        }
        final int lastDot = filename.lastIndexOf('.');
        return (lastDot > 0 && lastDot < filename.length() - 1) ? filename.substring(lastDot + 1) : "bin";
    }
}
