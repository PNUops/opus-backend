package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.application.processor.ImageProcessor;
import com.opus.opus.modules.file.application.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncImageProcessingService {

    private final ImageProcessor imageProcessor;
    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    @Async("imageTaskExecutor")
    @Transactional
    public void processAndStore(final byte[] imageBytes, final String relativePath, final Long fileId) {
        try {
            final byte[] processed = imageProcessor.process(imageBytes);
            fileStorage.store(processed, relativePath);
            fileRepository.findById(fileId)
                    .ifPresent(file -> file.updateIsWebpConverted(true));
        } catch (Exception e) {
            log.error("이미지 처리 실패 [fileId={}]: {}", fileId, e.getMessage(), e);
            cleanupFailedFile(relativePath, fileId);
        }
    }

    private void cleanupFailedFile(final String relativePath, final Long fileId) {
        try {
            fileStorage.delete(relativePath);
        } catch (Exception e) {
            log.warn("이미지 처리 실패 후 물리 파일 정리 실패 [path={}]: {}", relativePath, e.getMessage());
        }
        try {
            fileRepository.deleteById(fileId);
        } catch (Exception e) {
            log.error("이미지 처리 실패 후 DB 레코드 정리 실패 [fileId={}]: {}", fileId, e.getMessage(), e);
        }
    }

    public void deletePhysicalFile(final String relativePath) {
        fileStorage.delete(relativePath);
    }
}
