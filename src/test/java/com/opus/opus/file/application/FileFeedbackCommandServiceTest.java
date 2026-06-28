package com.opus.opus.file.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.opus.opus.file.FileFixture;
import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FileFeedbackCommandService;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileFeedback;
import com.opus.opus.modules.file.domain.dao.FileFeedbackRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileFeedbackCommandServiceTest extends FileModuleIntegrationTest {

    private static final Long FEEDBACK_ID = 1L;

    @Autowired
    private FileFeedbackCommandService fileFeedbackCommandService;

    @Autowired
    private FileFeedbackRepository fileFeedbackRepository;

    private FileFeedback saveExistingFile(final int fileOrder, final String name) {
        final File file = FileFixture.createFile(name, "files/" + name);
        return fileFeedbackRepository.save(FileFeedback.builder()
                .file(file)
                .feedbackId(FEEDBACK_ID)
                .fileOrder(fileOrder)
                .build());
    }

    private MockMultipartFile newFile(final String name) {
        return new MockMultipartFile("files", name, "application/pdf", "content".getBytes());
    }

    @Test
    @DisplayName("[성공] 첨부가 없는 피드백에 저장하면 fileOrder가 0부터 시작한다.")
    void 첨부가_없는_피드백에_저장하면_fileOrder가_0부터_시작한다() {
        fileFeedbackCommandService.storeFeedbackFiles(List.of(newFile("a.pdf")), FEEDBACK_ID);

        final List<Integer> orders = fileFeedbackRepository.findAllByFeedbackIdOrderByFileOrder(FEEDBACK_ID).stream()
                .map(FileFeedback::getFileOrder)
                .toList();

        assertThat(orders).containsExactly(0);
    }

    @Test
    @DisplayName("[성공] 중간 파일 삭제 후 추가 업로드해도 fileOrder가 중복되지 않는다.")
    void 중간_파일_삭제_후_추가_업로드해도_fileOrder가_중복되지_않는다() {
        saveExistingFile(0, "a.pdf");
        final FileFeedback middle = saveExistingFile(1, "b.pdf");
        saveExistingFile(2, "c.pdf");

        fileFeedbackCommandService.deleteFeedbackFiles(List.of(middle.getId()), FEEDBACK_ID);

        fileFeedbackCommandService.storeFeedbackFiles(List.of(newFile("d.pdf")), FEEDBACK_ID);

        final List<Integer> orders = fileFeedbackRepository.findAllByFeedbackIdOrderByFileOrder(FEEDBACK_ID).stream()
                .map(FileFeedback::getFileOrder)
                .toList();

        assertThat(orders).doesNotHaveDuplicates();
        assertThat(orders).contains(3);
    }

    @Test
    @DisplayName("[실패] 빈 파일이 섞여 있으면 디스크 저장 전에 예외가 발생한다.")
    void 빈_파일이_섞여_있으면_디스크_저장_전에_예외가_발생한다() {
        final MockMultipartFile valid = newFile("a.pdf");
        final MockMultipartFile empty = new MockMultipartFile("files", "b.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> fileFeedbackCommandService.storeFeedbackFiles(List.of(valid, empty), FEEDBACK_ID))
                .isInstanceOf(FileException.class)
                .hasMessage(FileExceptionType.EMPTY_FILE.errorMessage());

        verify(fileStorage, never()).store(any(), any());
    }

    @Test
    @DisplayName("[실패] 기존 첨부와 신규 첨부의 합이 5개를 넘으면 예외가 발생한다.")
    void 기존_첨부와_신규_첨부의_합이_5개를_넘으면_예외가_발생한다() {
        IntStream.range(0, 4).forEach(order -> saveExistingFile(order, "existing" + order + ".pdf"));

        final List<MultipartFile> newFiles = List.of(newFile("new1.pdf"), newFile("new2.pdf"));

        assertThatThrownBy(() -> fileFeedbackCommandService.storeFeedbackFiles(newFiles, FEEDBACK_ID))
                .isInstanceOf(FileException.class)
                .hasMessage(FileExceptionType.EXCEED_FEEDBACK_FILE_LIMIT.errorMessage());

        verify(fileStorage, never()).store(any(), any());
    }
}
