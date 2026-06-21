package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrphanImageCleanupScheduler {

    private final FileImageRepository fileImageRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 비동기 WebP 변환이 10분 내 완료되지 않은 레코드를 좀비로 판정하여 정리
    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void cleanupZombieRecords() {
        final LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        final List<FileImage> zombies = fileImageRepository
                .findAllByIsWebpConvertedFalseAndCreatedAtBefore(threshold);

        if (zombies.isEmpty()) {
            return;
        }

        log.warn("좀비 FileImage 레코드 {} 건 발견, 정리 시작", zombies.size());
        for (final FileImage zombie : zombies) {
            final String filePath = zombie.getFilePath();
            fileImageRepository.delete(zombie);
            eventPublisher.publishEvent(new PhysicalFileDeleteEvent(filePath));
        }
        log.info("좀비 FileImage 레코드 {} 건 정리 완료", zombies.size());
    }
}
