package com.opus.opus.file.application;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opus.opus.file.FileFixture;
import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.AsyncImageProcessingService;
import com.opus.opus.modules.file.application.convenience.FileImageTransactionHandler;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.AopTestUtils;

public class AsyncImageProcessingServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private AsyncImageProcessingService asyncImageProcessingService;

    @Autowired
    private FileImageRepository fileImageRepository;

    @MockitoBean
    private FileImageTransactionHandler fileImageTransactionHandler;

    private AsyncImageProcessingService target;

    @BeforeEach
    void setUpTarget() {
        target = AopTestUtils.getTargetObject(asyncImageProcessingService);
    }

    @Test
    @DisplayName("[성공] 이미지 처리 후 isWebpConverted가 true로 업데이트된다.")
    void 이미지_처리_후_isWebpConverted가_true로_업데이트된다() {
        when(imageProcessor.process(any())).thenReturn("processed".getBytes());
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileImageTransactionHandler).markWebpConverted(saved.getId());
    }

    @Test
    @DisplayName("[성공] imageProcessor.process() 후 fileStorage.store()가 순서대로 호출된다.")
    void 이미지_처리_후_저장이_순서대로_호출된다() {
        final byte[] processed = "processed".getBytes();
        when(imageProcessor.process(any())).thenReturn(processed);
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        final InOrder inOrder = inOrder(imageProcessor, fileStorage);
        inOrder.verify(imageProcessor).process(any());
        inOrder.verify(fileStorage).store(eq(processed), eq(saved.getFilePath()));
    }

    @Test
    @DisplayName("[실패] 변환 실패 시 좀비 레코드가 남지 않는다.")
    void 변환_실패_시_좀비_레코드가_남지_않는다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileImageTransactionHandler).deleteFileImageRecord(saved.getId());
    }

    @Test
    @DisplayName("[실패] 변환 실패 시 물리 파일 정리가 시도된다.")
    void 변환_실패_시_물리_파일_정리가_시도된다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileStorage).delete(saved.getFilePath());
    }

    @Test
    @DisplayName("[실패] 변환 실패 + 물리 파일 정리 실패 시 예외가 전파되지 않는다.")
    void 변환_실패_후_물리_파일_정리_실패_시_예외가_전파되지_않는다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        doThrow(new RuntimeException("삭제 실패")).when(fileStorage).delete(any());
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        assertThatNoException().isThrownBy(
                () -> target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId())
        );
    }

    @Test
    @DisplayName("[실패] 변환 실패 + 물리 파일 정리 실패 시에도 DB 레코드 정리는 시도된다.")
    void 변환_실패_후_물리_파일_정리_실패_시에도_DB_레코드는_정리된다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        doThrow(new RuntimeException("삭제 실패")).when(fileStorage).delete(any());
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileImageTransactionHandler).deleteFileImageRecord(saved.getId());
    }

    @Test
    @DisplayName("[성공] deletePhysicalFile은 fileStorage.delete()에 위임한다.")
    void deletePhysicalFile은_fileStorage_delete에_위임한다() {
        final String path = "files/2024-01-01/test.webp";

        target.deletePhysicalFile(path);

        verify(fileStorage).delete(path);
    }

    @Test
    @DisplayName("[실패] 파일 저장 실패 시 DB 레코드가 정리된다.")
    void 파일_저장_실패_시_DB_레코드가_정리된다() {
        final byte[] processed = "processed".getBytes();
        when(imageProcessor.process(any())).thenReturn(processed);
        doThrow(new RuntimeException("저장 실패")).when(fileStorage).store(any(), any());
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileImageTransactionHandler).deleteFileImageRecord(saved.getId());
    }

    @Test
    @DisplayName("[실패] markWebpConverted 실패 시 DB 레코드와 물리 파일이 정리된다.")
    void markWebpConverted_실패_시_DB_레코드와_물리_파일이_정리된다() {
        final byte[] processed = "processed".getBytes();
        when(imageProcessor.process(any())).thenReturn(processed);
        doThrow(new RuntimeException("업데이트 실패")).when(fileImageTransactionHandler).markWebpConverted(any());
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        target.processAndStoreForFileImage("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileStorage).delete(saved.getFilePath());
        verify(fileImageTransactionHandler).deleteFileImageRecord(saved.getId());
    }
}
