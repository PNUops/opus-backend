package com.opus.opus.global.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class AsyncConfigTest {

    @Test
    @DisplayName("[실패] 메일 발송 스레드와 큐가 모두 차면 작업을 거절한다.")
    void 메일_발송_스레드와_큐가_모두_차면_작업을_거절한다() {
        final ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new AsyncConfig().mailTaskExecutor();
        final CountDownLatch blockLatch = new CountDownLatch(1);

        try {
            for (int i = 0; i < 505; i++) {
                executor.execute(() -> awaitQuietly(blockLatch));
            }

            assertThatThrownBy(() -> executor.execute(() -> {
            })).isInstanceOf(TaskRejectedException.class);
        } finally {
            blockLatch.countDown();
            executor.shutdown();
        }
    }

    private static void awaitQuietly(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
