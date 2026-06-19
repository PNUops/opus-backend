package com.opus.opus.file.application.event;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.opus.opus.modules.file.application.AsyncImageProcessingService;
import com.opus.opus.modules.file.application.event.ImageProcessingEvent;
import com.opus.opus.modules.file.application.event.ImageProcessingEventListener;
import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageProcessingEventListenerTest {

    @Mock
    private AsyncImageProcessingService asyncImageProcessingService;

    @InjectMocks
    private ImageProcessingEventListener listener;

    @Test
    @DisplayName("[성공] ImageProcessingEvent 수신 시 processAndStoreForFileImage에 올바른 파라미터로 위임한다.")
    void handleImageProcessing_올바른_파라미터로_위임한다() {
        final byte[] imageBytes = "image".getBytes();
        final String relativePath = "files/2024-01-01/test.jpg";
        final Long fileImageId = 42L;
        final ImageProcessingEvent event = new ImageProcessingEvent(imageBytes, relativePath, fileImageId);

        listener.handleImageProcessing(event);

        verify(asyncImageProcessingService).processAndStoreForFileImage(imageBytes, relativePath, fileImageId);
    }

    @Test
    @DisplayName("[성공] PhysicalFileDeleteEvent 수신 시 deletePhysicalFile에 위임한다.")
    void handlePhysicalFileDelete_위임한다() {
        final String relativePath = "files/2024-01-01/test.webp";
        final PhysicalFileDeleteEvent event = new PhysicalFileDeleteEvent(relativePath);

        listener.handlePhysicalFileDelete(event);

        verify(asyncImageProcessingService).deletePhysicalFile(relativePath);
    }

    @Test
    @DisplayName("[실패] handlePhysicalFileDelete에서 예외 발생 시 예외가 전파되지 않는다.")
    void handlePhysicalFileDelete_예외_발생_시_전파되지_않는다() {
        doThrow(new RuntimeException("삭제 실패")).when(asyncImageProcessingService).deletePhysicalFile(any());
        final PhysicalFileDeleteEvent event = new PhysicalFileDeleteEvent("files/2024-01-01/test.webp");

        assertThatNoException().isThrownBy(() -> listener.handlePhysicalFileDelete(event));
    }
}
