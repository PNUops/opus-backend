package com.opus.opus.global.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    private static final long MAIL_IN_FLIGHT_AWAIT_SECONDS = 15L;

    private final long mailShutdownAwaitSeconds;

    public AsyncConfig(@Value("${async.mail.shutdown-await-seconds:30}") final long mailShutdownAwaitSeconds) {
        this.mailShutdownAwaitSeconds = mailShutdownAwaitSeconds;
    }

    @Bean(name = "imageTaskExecutor")
    public Executor imageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(30);
        executor.setThreadNamePrefix("ImageAsync-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void onApplicationEvent(final ContextClosedEvent event) {
                super.onApplicationEvent(event);
                awaitQueuedTasks(getThreadPoolExecutor());
            }
        };
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("MailAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    private void awaitQueuedTasks(final ThreadPoolExecutor executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(mailShutdownAwaitSeconds, TimeUnit.SECONDS)) {
                dropQueuedTasks(executor);
                executor.awaitTermination(MAIL_IN_FLIGHT_AWAIT_SECONDS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void dropQueuedTasks(final ThreadPoolExecutor executor) {
        final List<Runnable> droppedTasks = new ArrayList<>();
        executor.getQueue().drainTo(droppedTasks);
        log.warn("메일 발송 큐 종료 대기 시간 초과 [미발송={}건]", droppedTasks.size());
    }
}
