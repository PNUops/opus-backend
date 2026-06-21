package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileComment;
import com.opus.opus.modules.file.domain.dao.FileCommentRepository;
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
public class FileCommentCommandService {

    private final FileCommentRepository fileCommentRepository;
    private final FilePathGenerator filePathGenerator;
    private final FileStorage fileStorage;
    private final ApplicationEventPublisher eventPublisher;

    public void storeCommentFiles(final List<MultipartFile> multipartFiles, final Long commentId) {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return;
        }

        int fileOrder = fileCommentRepository.findAllByCommentIdOrderByFileOrder(commentId).size();
        for (final MultipartFile multipartFile : multipartFiles) {
            storeCommentFile(multipartFile, commentId, fileOrder++);
        }
    }

    public void deleteCommentFiles(final List<Long> fileCommentIds, final Long commentId) {
        if (fileCommentIds == null || fileCommentIds.isEmpty()) {
            return;
        }

        for (final Long fileCommentId : fileCommentIds) {
            final FileComment fileComment = fileCommentRepository.findById(fileCommentId)
                    .filter(file -> file.getCommentId().equals(commentId))
                    .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND, "삭제할 파일을 찾을 수 없습니다. ID=" + fileCommentId));
            deleteWithPhysicalFile(fileComment);
        }
    }

    public void deleteAllByCommentId(final Long commentId) {
        final List<FileComment> fileComments = fileCommentRepository.findAllByCommentIdOrderByFileOrder(commentId);
        fileComments.forEach(this::deleteWithPhysicalFile);
    }

    private void storeCommentFile(final MultipartFile multipartFile, final Long commentId, final int fileOrder) {
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

            fileCommentRepository.save(FileComment.builder()
                    .file(file)
                    .commentId(commentId)
                    .fileOrder(fileOrder)
                    .build());

            fileStorage.store(fileBytes, relativePath);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private void deleteWithPhysicalFile(final FileComment fileComment) {
        final String filePath = fileComment.getFilePath();
        fileCommentRepository.delete(fileComment);
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
