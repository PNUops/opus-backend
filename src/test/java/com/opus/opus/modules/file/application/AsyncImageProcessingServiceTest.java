package com.opus.opus.modules.file.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.file.FileFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

public class AsyncImageProcessingServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private AsyncImageProcessingService asyncImageProcessingService;

    @Autowired
    private FileRepository fileRepository;

    private AsyncImageProcessingService target;

    @BeforeEach
    void setUpTarget() {
        target = AopTestUtils.getTargetObject(asyncImageProcessingService);
    }

    @Test
    @DisplayName("[성공] 이미지 처리 후 isWebpConverted가 true로 업데이트된다.")
    void 이미지_처리_후_isWebpConverted가_true로_업데이트된다() {
        when(imageProcessor.process(any())).thenReturn("processed".getBytes());
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId());

        final File updated = fileRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getIsWebpConverted()).isTrue();
    }

    @Test
    @DisplayName("[성공] imageProcessor.process() 후 fileStorage.store()가 순서대로 호출된다.")
    void 이미지_처리_후_저장이_순서대로_호출된다() {
        final byte[] processed = "processed".getBytes();
        when(imageProcessor.process(any())).thenReturn(processed);
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(imageProcessor).process(any());
        verify(fileStorage).store(eq(processed), eq(saved.getFilePath()));
    }

    @Test
    @DisplayName("[엣지] 변환 실패 시 좀비 레코드가 남지 않는다.")
    void 변환_실패_시_좀비_레코드가_남지_않는다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId());

        assertThat(fileRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("[엣지] 변환 실패 시 물리 파일 정리가 시도된다.")
    void 변환_실패_시_물리_파일_정리가_시도된다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId());

        verify(fileStorage).delete(saved.getFilePath());
    }

    @Test
    @DisplayName("[엣지] 변환 실패 + 물리 파일 정리 실패 시 예외가 전파되지 않는다.")
    void 변환_실패_후_물리_파일_정리_실패_시_예외가_전파되지_않는다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        doThrow(new RuntimeException("삭제 실패")).when(fileStorage).delete(any());
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        assertThatNoException().isThrownBy(
                () -> target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId())
        );
    }

    @Test
    @DisplayName("[엣지] 변환 실패 + 물리 파일 정리 실패 시에도 DB 레코드 정리는 시도된다.")
    void 변환_실패_후_물리_파일_정리_실패_시에도_DB_레코드는_정리된다() {
        when(imageProcessor.process(any())).thenThrow(new RuntimeException("변환 실패"));
        doThrow(new RuntimeException("삭제 실패")).when(fileStorage).delete(any());
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        target.processAndStore("original".getBytes(), saved.getFilePath(), saved.getId());

        assertThat(fileRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] deletePhysicalFile은 fileStorage.delete()에 위임한다.")
    void deletePhysicalFile은_fileStorage_delete에_위임한다() {
        final String path = "files/2024-01-01/test.webp";

        target.deletePhysicalFile(path);

        verify(fileStorage).delete(path);
    }
}
