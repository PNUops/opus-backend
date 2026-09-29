package com.opus.opus.contest.application.convenience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.convenience.ContestCategorySortConvenience;
import com.opus.opus.modules.contest.domain.SidebarCategorySort;
import com.opus.opus.modules.contest.domain.dao.SidebarCategorySortRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

public class ContestCategorySortConvenienceTest extends IntegrationTest {

    @Autowired
    private ContestCategorySortConvenience contestCategorySortConvenience;
    @Autowired
    private SidebarCategorySortRepository sidebarCategorySortRepository;

    @BeforeTransaction
    void 커밋된_사이드바_정렬_설정을_정리한다() {
        sidebarCategorySortRepository.deleteAllInBatch();
    }

    @AfterTransaction
    void 테스트에서_커밋된_사이드바_정렬_설정을_정리한다() {
        sidebarCategorySortRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("[성공] sidebar_category_sort는 유니크 제약으로 두 번째 행 저장이 실패한다.")
    void sidebar_category_sort는_유니크_제약으로_두번째_행_저장이_실패한다() {
        sidebarCategorySortRepository.save(SidebarCategorySort.createDefault());

        assertThatThrownBy(() -> sidebarCategorySortRepository.save(SidebarCategorySort.createDefault()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("[성공] 동시에 두 스레드가 getOrCreateSidebarCategorySort를 호출해도 행이 하나만 생성된다.")
    void 동시에_getOrCreateSidebarCategorySort를_호출해도_행이_하나만_생성된다() throws InterruptedException {
        final int threadCount = 2;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch readyLatch = new CountDownLatch(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    contestCategorySortConvenience.getOrCreateSidebarCategorySort();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(sidebarCategorySortRepository.count()).isEqualTo(1);
    }
}
