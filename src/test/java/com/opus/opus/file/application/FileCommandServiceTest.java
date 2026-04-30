package com.opus.opus.file.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EMPTY_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.file.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class FileCommandServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FileCommandService fileCommandService;

    @Autowired
    private FileRepository fileRepository;

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());
    }

    @Test
    @DisplayName("[성공] 정상 이미지 저장 시 DB에 File 엔티티가 생성된다.")
    void 정상_이미지_저장_시_DB에_File_엔티티가_생성된다() {
        final File saved = fileCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("[성공] 저장된 File의 isWebpConverted 초기값은 false이다.")
    void 저장된_File의_isWebpConverted_초기값은_false이다() {
        final File saved = fileCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getIsWebpConverted()).isFalse();
    }

    @Test
    @DisplayName("[성공] 생성된 파일 경로가 files/{날짜}/{UUID}.webp 형식이다.")
    void 생성된_파일_경로가_지정된_형식이다() {
        final File saved = fileCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getFilePath()).matches("files/\\d{4}-\\d{2}-\\d{2}/[\\w-]+\\.webp");
    }

    @Test
    @DisplayName("[실패] null 파일 저장 시 EMPTY_FILE 예외가 발생한다.")
    void null_파일_저장_시_예외가_발생한다() {
        assertThatThrownBy(() -> fileCommandService.storeImageFile(null, 1L, TEAM, POSTER))
                .isInstanceOf(FileException.class)
                .hasMessage(EMPTY_FILE.errorMessage());
    }

    @Test
    @DisplayName("[실패] 빈 파일 저장 시 EMPTY_FILE 예외가 발생한다.")
    void 빈_파일_저장_시_예외가_발생한다() {
        final MockMultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileCommandService.storeImageFile(emptyFile, 1L, TEAM, POSTER))
                .isInstanceOf(FileException.class)
                .hasMessage(EMPTY_FILE.errorMessage());
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 기존 파일 없으면 새 파일만 저장된다.")
    void replaceImageFile_기존_파일_없으면_새_파일만_저장된다() {
        final File saved = fileCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileRepository.findById(saved.getId())).isPresent();
        assertThat(fileRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 기존 파일이 있으면 새 파일 저장 후 기존 파일이 DB에서 삭제된다.")
    void replaceImageFile_기존_파일이_있으면_새_저장_후_기존_삭제된다() {
        final File existing = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        final File saved = fileCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileRepository.findById(existing.getId())).isEmpty();
        assertThat(fileRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 반환값이 새로 저장된 File이다.")
    void replaceImageFile_반환값이_새로_저장된_File이다() {
        fileRepository.save(FileFixture.createTeamPosterFile(1L));

        final File saved = fileCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getName()).isEqualTo("photo.jpg");
    }

    @Test
    @DisplayName("[성공] deleteFile - DB 레코드가 삭제되고 물리 파일 삭제가 위임된다.")
    void deleteFile_DB_레코드_삭제_및_물리_파일_삭제_위임된다() {
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        fileCommandService.deleteFile(saved.getId());

        assertThat(fileRepository.findById(saved.getId())).isEmpty();
        verify(fileStorage).delete(saved.getFilePath());
    }

    @Test
    @DisplayName("[실패] deleteFile - 존재하지 않는 ID 삭제 시 FileException이 발생한다.")
    void deleteFile_존재하지_않는_ID_삭제_시_예외가_발생한다() {
        final Long nonExistentId = 999999L;

        assertThatThrownBy(() -> fileCommandService.deleteFile(nonExistentId))
                .isInstanceOf(FileException.class);
    }

    @Test
    @DisplayName("[성공] 저장된 파일의 referenceId와 imageType이 올바르게 저장된다.")
    void 저장된_파일의_메타데이터가_올바르게_저장된다() {
        final File saved = fileCommandService.storeImageFile(imageFile(), 42L, TEAM, THUMBNAIL);

        final File found = fileRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getReferenceId()).isEqualTo(42L);
        assertThat(found.getReferenceType()).isEqualTo(TEAM);
        assertThat(found.getImageType()).isEqualTo(THUMBNAIL);
    }
}
