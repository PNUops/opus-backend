package com.opus.opus.file.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FileDocumentQueryServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FileDocumentQueryService fileDocumentQueryService;

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Test
    @DisplayName("[성공] 제출 파일 다운로드 정보를 조회한다.")
    void 제출_파일_다운로드_정보를_조회한다() {
        when(fileStorage.load(any())).thenReturn("doc-content".getBytes());
        final FileDocument saved = fileDocumentRepository.save(FileDocument.builder()
                .file(File.create("발표자료.pdf", "files/2026-06-24/a.pdf", "application/pdf", 1048576L))
                .submissionId(12L)
                .fileOrder(0)
                .build());

        final DocumentFileDownload result = fileDocumentQueryService.download(saved.getId());

        assertThat(result.fileName()).isEqualTo("발표자료.pdf");
        assertThat(result.mimeType()).isEqualTo("application/pdf");
        assertThat(result.fileSize()).isEqualTo(1048576L);
        assertThat(result.submissionId()).isEqualTo(12L);
        assertThat(result.resource()).isNotNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 파일 다운로드 시 예외가 발생한다.")
    void 존재하지_않는_제출_파일_다운로드_시_예외가_발생한다() {
        assertThatThrownBy(() -> fileDocumentQueryService.download(999999L))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_FOUND.errorMessage());
    }
}
