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
}
