package com.opus.opus.modules.file.application.event;

import com.opus.opus.modules.file.application.AsyncImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageProcessingEventListener {

    private final AsyncImageProcessingService asyncImageProcessingService;

    @Async("imageTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleImageProcessing(final ImageProcessingEvent event) {
        asyncImageProcessingService.processAndStoreForFileImage(
                event.imageBytes(), event.relativePath(), event.fileImageId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePhysicalFileDelete(final PhysicalFileDeleteEvent event) {
        try {
            asyncImageProcessingService.deletePhysicalFile(event.relativePath());
        } catch (Exception e) {
            log.warn("물리 파일 삭제 실패 [path={}]: {}", event.relativePath(), e.getMessage());
        }
    }
}
