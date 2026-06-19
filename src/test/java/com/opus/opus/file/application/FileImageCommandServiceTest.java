package com.opus.opus.file.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EMPTY_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.file.FileFixture;
import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FileImageCommandService;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import com.opus.opus.modules.file.exception.FileException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class FileImageCommandServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FileImageCommandService fileImageCommandService;

    @Autowired
    private FileImageRepository fileImageRepository;

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());
    }

    @Test
    @DisplayName("[성공] 정상 이미지 저장 시 DB에 FileImage 엔티티가 생성된다.")
    void 정상_이미지_저장_시_DB에_FileImage_엔티티가_생성된다() {
        final FileImage saved = fileImageCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileImageRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("[성공] 저장된 FileImage의 isWebpConverted 초기값은 false이다.")
    void 저장된_FileImage의_isWebpConverted_초기값은_false이다() {
        final FileImage saved = fileImageCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getIsWebpConverted()).isFalse();
    }

    @Test
    @DisplayName("[성공] 생성된 파일 경로가 files/{날짜}/{UUID}.webp 형식이다.")
    void 생성된_파일_경로가_지정된_형식이다() {
        final FileImage saved = fileImageCommandService.storeImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getFilePath()).matches("files/\\d{4}-\\d{2}-\\d{2}/[\\w-]+\\.webp");
    }

    @Test
    @DisplayName("[실패] null 파일 저장 시 EMPTY_FILE 예외가 발생한다.")
    void null_파일_저장_시_예외가_발생한다() {
        assertThatThrownBy(() -> fileImageCommandService.storeImageFile(null, 1L, TEAM, POSTER))
                .isInstanceOf(FileException.class)
                .hasMessage(EMPTY_FILE.errorMessage());
    }

    @Test
    @DisplayName("[실패] 빈 파일 저장 시 EMPTY_FILE 예외가 발생한다.")
    void 빈_파일_저장_시_예외가_발생한다() {
        final MockMultipartFile emptyFile = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileImageCommandService.storeImageFile(emptyFile, 1L, TEAM, POSTER))
                .isInstanceOf(FileException.class)
                .hasMessage(EMPTY_FILE.errorMessage());
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 기존 파일 없으면 새 파일만 저장된다.")
    void replaceImageFile_기존_파일_없으면_새_파일만_저장된다() {
        final FileImage saved = fileImageCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileImageRepository.findById(saved.getId())).isPresent();
        assertThat(fileImageRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 기존 파일이 있으면 새 파일 저장 후 기존 파일이 DB에서 삭제된다.")
    void replaceImageFile_기존_파일이_있으면_새_저장_후_기존_삭제된다() {
        final FileImage existing = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        final FileImage saved = fileImageCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(fileImageRepository.findById(existing.getId())).isEmpty();
        assertThat(fileImageRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("[성공] replaceImageFile - 반환값이 새로 저장된 FileImage이다.")
    void replaceImageFile_반환값이_새로_저장된_FileImage이다() {
        fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        final FileImage saved = fileImageCommandService.replaceImageFile(imageFile(), 1L, TEAM, POSTER);

        assertThat(saved.getFile().getName()).isEqualTo("photo.jpg");
    }

    @Test
    @DisplayName("[성공] deleteImageFile - DB 레코드가 삭제된다.")
    void deleteImageFile_DB_레코드_삭제_및_물리_파일_삭제_위임된다() {
        final FileImage saved = fileImageRepository.save(FileFixture.createTeamPosterFileImage(1L));

        fileImageCommandService.deleteImageFile(saved.getId());

        assertThat(fileImageRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] deleteImageFile - 존재하지 않는 ID 삭제 시 FileException이 발생한다.")
    void deleteImageFile_존재하지_않는_ID_삭제_시_예외가_발생한다() {
        final Long nonExistentId = 999999L;

        assertThatThrownBy(() -> fileImageCommandService.deleteImageFile(nonExistentId))
                .isInstanceOf(FileException.class);
    }

    @Test
    @DisplayName("[성공] 저장된 파일의 referenceId와 imageType이 올바르게 저장된다.")
    void 저장된_파일의_메타데이터가_올바르게_저장된다() {
        final FileImage saved = fileImageCommandService.storeImageFile(imageFile(), 42L, TEAM, THUMBNAIL);

        final FileImage found = fileImageRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getReferenceId()).isEqualTo(42L);
        assertThat(found.getReferenceType()).isEqualTo(TEAM);
        assertThat(found.getImageType()).isEqualTo(THUMBNAIL);
    }
}
