package com.opus.opus.file.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FileDocumentCommandService;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class FileDocumentCommandServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FileDocumentCommandService fileDocumentCommandService;

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    private MockMultipartFile pdf(final String name) {
        return new MockMultipartFile("files", name, "application/pdf", "content".getBytes());
    }

    @Test
    @DisplayName("[성공] 기존 파일이 없으면 fileOrder가 1부터 순서대로 저장된다.")
    void 기존_파일이_없으면_1부터_저장된다() {
        final Long submissionId = 100L;

        fileDocumentCommandService.storeDocumentFiles(submissionId, List.of(pdf("a.pdf"), pdf("b.pdf")));

        assertThat(fileDocumentRepository.findAllBySubmissionIdOrderByFileOrder(submissionId))
                .extracting(FileDocument::getFileOrder)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("[성공] 기존 파일이 있으면 마지막 fileOrder 다음 번호부터 저장된다.")
    void 기존_파일_다음_번호부터_저장된다() {
        final Long submissionId = 200L;
        fileDocumentCommandService.storeDocumentFiles(submissionId, List.of(pdf("a.pdf"), pdf("b.pdf")));

        fileDocumentCommandService.storeDocumentFiles(submissionId, List.of(pdf("c.pdf")));

        assertThat(fileDocumentRepository.findAllBySubmissionIdOrderByFileOrder(submissionId))
                .extracting(FileDocument::getFileOrder)
                .containsExactly(1, 2, 3);
    }
}
