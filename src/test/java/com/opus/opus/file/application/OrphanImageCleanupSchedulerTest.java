package com.opus.opus.file.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.opus.opus.file.FileFixture;
import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.OrphanImageCleanupScheduler;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class OrphanImageCleanupSchedulerTest extends FileModuleIntegrationTest {

    @Autowired
    private OrphanImageCleanupScheduler orphanImageCleanupScheduler;

    @Autowired
    private FileImageRepository fileImageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("[성공] 변환 완료된 이미지만 있으면 정리 대상이 없다.")
    void 변환_완료된_이미지만_있으면_정리_대상이_없다() {
        // Given
        final FileImage fileImage = FileFixture.createTeamPosterFileImage(1L);
        fileImage.markWebpConverted();
        fileImageRepository.save(fileImage);
        entityManager.flush();

        // When
        orphanImageCleanupScheduler.cleanupZombieRecords();

        // Then
        verifyNoInteractions(fileStorage);
        assertThat(fileImageRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 변환 미완료 이미지가 있어도 10분이 지나지 않으면 정리하지 않는다.")
    void 변환_미완료_이미지가_있어도_10분이_지나지_않으면_정리하지_않는다() {
        // Given
        final FileImage fileImage = FileFixture.createTeamPosterFileImage(1L);
        fileImageRepository.save(fileImage);
        entityManager.flush();

        // When
        orphanImageCleanupScheduler.cleanupZombieRecords();

        // Then
        verify(fileStorage, times(0)).delete(any());
        assertThat(fileImageRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 10분 이상 변환 미완료 상태인 좀비 레코드는 삭제된다.")
    void 좀비_레코드가_삭제된다() {
        // Given
        final FileImage fileImage = FileFixture.createTeamPosterFileImage(1L);
        fileImageRepository.save(fileImage);
        entityManager.flush();

        entityManager.createNativeQuery(
                        "UPDATE file_image SET created_at = :threshold WHERE id = :id")
                .setParameter("threshold", LocalDateTime.now().minusMinutes(11))
                .setParameter("id", fileImage.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // When
        orphanImageCleanupScheduler.cleanupZombieRecords();

        // Then
        verify(fileStorage, times(1)).delete(fileImage.getFilePath());
        assertThat(fileImageRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 정상 레코드는 그대로 두고 좀비 레코드만 삭제된다.")
    void 정상_레코드는_그대로_두고_좀비_레코드만_삭제된다() {
        // Given
        final FileImage normalImage = FileFixture.createTeamPosterFileImage(1L);
        normalImage.markWebpConverted();
        fileImageRepository.save(normalImage);

        final FileImage zombieImage = FileFixture.createTeamThumbnailFileImage(1L);
        fileImageRepository.save(zombieImage);
        entityManager.flush();

        entityManager.createNativeQuery(
                        "UPDATE file_image SET created_at = :threshold WHERE id = :id")
                .setParameter("threshold", LocalDateTime.now().minusMinutes(11))
                .setParameter("id", zombieImage.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // When
        orphanImageCleanupScheduler.cleanupZombieRecords();

        // Then
        verify(fileStorage, times(1)).delete(zombieImage.getFilePath());
        assertThat(fileImageRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] 물리 파일 삭제 실패해도 DB 레코드는 삭제된다.")
    void 물리_파일_삭제_실패해도_DB_레코드는_삭제된다() {
        // Given
        final FileImage fileImage = FileFixture.createTeamPosterFileImage(1L);
        fileImageRepository.save(fileImage);
        entityManager.flush();

        entityManager.createNativeQuery(
                        "UPDATE file_image SET created_at = :threshold WHERE id = :id")
                .setParameter("threshold", LocalDateTime.now().minusMinutes(11))
                .setParameter("id", fileImage.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        doThrow(new RuntimeException("물리 파일 삭제 실패")).when(fileStorage).delete(any());

        // When
        orphanImageCleanupScheduler.cleanupZombieRecords();

        // Then
        assertThat(fileImageRepository.count()).isEqualTo(0);
    }
}
