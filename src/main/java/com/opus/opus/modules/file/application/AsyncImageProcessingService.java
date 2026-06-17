package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.convenience.FileImageTransactionHandler;
import com.opus.opus.modules.file.application.convenience.FileTransactionHandler;
import com.opus.opus.modules.file.application.processor.ImageProcessor;
import com.opus.opus.modules.file.application.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncImageProcessingService {

    private final ImageProcessor imageProcessor;
    private final FileStorage fileStorage;
    private final FileTransactionHandler fileTransactionHandler;
    private final FileImageTransactionHandler fileImageTransactionHandler;

    @Async("imageTaskExecutor")
    public void processAndStore(final byte[] imageBytes, final String relativePath, final Long fileId) {
        try {
            final byte[] processed = imageProcessor.process(imageBytes);
            fileStorage.store(processed, relativePath);
            fileTransactionHandler.markWebpConverted(fileId);
        } catch (Exception e) {
            log.error("이미지 처리 실패 [fileId={}]: {}", fileId, e.getMessage(), e);
            cleanupFailedFile(relativePath, fileId);
        }
    }

    @Async("imageTaskExecutor")
    public void processAndStoreForFileImage(final byte[] imageBytes, final String relativePath, final Long fileImageId) {
        try {
            final byte[] processed = imageProcessor.process(imageBytes);
            fileStorage.store(processed, relativePath);
            fileImageTransactionHandler.markWebpConverted(fileImageId);
        } catch (Exception e) {
            log.error("이미지 처리 실패 [fileImageId={}]: {}", fileImageId, e.getMessage(), e);
            cleanupFailedFileImage(relativePath, fileImageId);
        }
    }

    public void deletePhysicalFile(final String relativePath) {
        fileStorage.delete(relativePath);
    }

    private void cleanupFailedFile(final String relativePath, final Long fileId) {
        try {
            fileStorage.delete(relativePath);
        } catch (Exception e) {
            log.warn("이미지 처리 실패 후 물리 파일 정리 실패 [path={}]: {}", relativePath, e.getMessage());
        }
        try {
            fileTransactionHandler.deleteFileRecord(fileId);
        } catch (Exception e) {
            log.error("이미지 처리 실패 후 DB 레코드 정리 실패 [fileId={}]: {}", fileId, e.getMessage(), e);
        }
    }

    private void cleanupFailedFileImage(final String relativePath, final Long fileImageId) {
        try {
            fileStorage.delete(relativePath);
        } catch (Exception e) {
            log.warn("이미지 처리 실패 후 물리 파일 정리 실패 [path={}]: {}", relativePath, e.getMessage());
        }
        try {
            fileImageTransactionHandler.deleteFileImageRecord(fileImageId);
        } catch (Exception e) {
            log.error("이미지 처리 실패 후 DB 레코드 정리 실패 [fileImageId={}]: {}", fileImageId, e.getMessage(), e);
        }
    }
}
