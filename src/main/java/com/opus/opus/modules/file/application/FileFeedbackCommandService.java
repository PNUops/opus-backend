package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileFeedback;
import com.opus.opus.modules.file.domain.dao.FileFeedbackRepository;
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
public class FileFeedbackCommandService {

    private final FileFeedbackRepository fileFeedbackRepository;
    private final FilePathGenerator filePathGenerator;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher eventPublisher;

    public void storeFeedbackFiles(final List<MultipartFile> multipartFiles, final Long feedbackId) {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return;
        }

        int fileOrder = fileFeedbackRepository.findAllByFeedbackIdOrderByFileOrder(feedbackId).size();
        for (final MultipartFile multipartFile : multipartFiles) {
            storeFeedbackFile(multipartFile, feedbackId, fileOrder++);
        }
    }

    public void deleteFeedbackFiles(final List<Long> fileIds, final Long feedbackId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        for (final Long fileId : fileIds) {
            final FileFeedback fileFeedback = fileFeedbackRepository.findByIdAndFeedbackId(fileId, feedbackId)
                    .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND, "삭제할 파일을 찾을 수 없습니다. ID=" + fileId));
            deleteWithPhysicalFile(fileFeedback);
        }
    }

    private void storeFeedbackFile(final MultipartFile multipartFile, final Long feedbackId, final int fileOrder) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException(FileExceptionType.EMPTY_FILE);
        }

        try {
            final byte[] fileBytes = multipartFile.getBytes();
            final String extension = extractExtension(multipartFile.getOriginalFilename());
            final String relativePath = filePathGenerator.generate(extension);
            final String mimeType = multipartFile.getContentType() != null
                    ? multipartFile.getContentType() : "application/octet-stream";

            final File file = File.create(multipartFile.getOriginalFilename(), relativePath, mimeType,
                    multipartFile.getSize());

            fileFeedbackRepository.save(FileFeedback.builder()
                    .file(file)
                    .feedbackId(feedbackId)
                    .fileOrder(fileOrder)
                    .build());

            fileStorage.store(fileBytes, relativePath);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private void deleteWithPhysicalFile(final FileFeedback fileFeedback) {
        final String filePath = fileFeedback.getFilePath();
        fileFeedbackRepository.delete(fileFeedback);
        eventPublisher.publishEvent(new PhysicalFileDeleteEvent(filePath));
    }

    private String extractExtension(final String filename) {
        if (filename == null || filename.isBlank()) {
            return "bin";
        }
        final int lastDot = filename.lastIndexOf('.');
        return (lastDot > 0 && lastDot < filename.length() - 1) ? filename.substring(lastDot + 1) : "bin";
    }
}
