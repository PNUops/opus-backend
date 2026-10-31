package com.opus.opus.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
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

    @Test
    @DisplayName("[성공] 컨텍스트 종료 이벤트 처리가 끝나면 큐에 남은 메일 발송 작업도 모두 완료되어 있다.")
    void 컨텍스트_종료_이벤트_처리가_끝나면_큐에_남은_메일_발송_작업도_모두_완료되어_있다() {
        final ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new AsyncConfig().mailTaskExecutor();
        final AtomicInteger completedCount = new AtomicInteger();

        for (int i = 0; i < 20; i++) {
            executor.execute(() -> {
                sleepQuietly(100);
                completedCount.incrementAndGet();
            });
        }
        executor.onApplicationEvent(new ContextClosedEvent(new GenericApplicationContext()));

        assertThat(completedCount.get()).isEqualTo(20);
    }

    private static void awaitQuietly(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepQuietly(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
