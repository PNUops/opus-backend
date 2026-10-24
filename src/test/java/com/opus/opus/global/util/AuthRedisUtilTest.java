package com.opus.opus.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.helper.IntegrationTest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthRedisUtilTest extends IntegrationTest {

    private static final String KEY = "email:auth:count:redis-util@pusan.ac.kr";

    @BeforeEach
    void setUp() {
        authRedisUtil.delete(KEY);
    }

    @Test
    @DisplayName("[성공] 키가 있으면 값을 1 감소시킨다.")
    void 키가_있으면_값을_1_감소시킨다() {
        authRedisUtil.incrementWithTtl(KEY, 1, TimeUnit.DAYS);
        authRedisUtil.incrementWithTtl(KEY, 1, TimeUnit.DAYS);

        authRedisUtil.decrementIfExists(KEY);

        assertThat(authRedisUtil.get(KEY)).isEqualTo("1");
    }

    @Test
    @DisplayName("[성공] 키가 없으면 키를 생성하지 않는다.")
    void 키가_없으면_키를_생성하지_않는다() {
        authRedisUtil.decrementIfExists(KEY);

        assertThat(authRedisUtil.exists(KEY)).isFalse();
    }
}
