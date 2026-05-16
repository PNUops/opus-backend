package com.opus.opus.file.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.modules.file.application.dto.FileResource;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.file.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FileQueryServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FileQueryService fileQueryService;

    @Autowired
    private FileRepository fileRepository;

    @Test
    @DisplayName("[성공] 파일 ID로 조회 시 FileResource가 반환된다.")
    void 파일_ID로_조회_시_FileResource가_반환된다() {
        when(fileStorage.load(any())).thenReturn("content".getBytes());
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        final FileResource result = fileQueryService.findFileAndType(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.resource()).isNotNull();
    }

    @Test
    @DisplayName("[성공] .webp 파일 조회 시 MIME 타입이 image/webp이다.")
    void webp_파일_조회_시_MIME_타입이_image_webp이다() {
        when(fileStorage.load(any())).thenReturn("content".getBytes());
        final File saved = fileRepository.save(FileFixture.createTeamPosterFile(1L));

        final FileResource result = fileQueryService.findFileAndType(saved.getId());

        assertThat(result.mimeType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 파일 ID 조회 시 NOT_EXISTS_MATCHING_IMAGE_ID 예외가 발생한다.")
    void 존재하지_않는_파일_ID_조회_시_예외가_발생한다() {
        final Long nonExistentId = 999999L;

        assertThatThrownBy(() -> fileQueryService.findFileAndType(nonExistentId))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_EXISTS_MATCHING_IMAGE_ID.errorMessage());
    }

    @Test
    @DisplayName("[성공] 기본 썸네일 조회 시 default_thumbnail.jpg 경로로 fileStorage.load()가 호출된다.")
    void 기본_썸네일_조회_시_올바른_경로로_load가_호출된다() {
        when(fileStorage.load(any())).thenReturn("content".getBytes());

        fileQueryService.findDefaultThumbnail();

        verify(fileStorage).load("default_thumbnail.jpg");
    }
}
