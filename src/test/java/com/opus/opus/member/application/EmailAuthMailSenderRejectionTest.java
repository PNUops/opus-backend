package com.opus.opus.member.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;

import com.opus.opus.global.config.AsyncConfig;
import com.opus.opus.global.util.AuthRedisUtil;
import com.opus.opus.global.util.MailUtil;
import com.opus.opus.modules.member.application.EmailAuthMailSender;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {AsyncConfig.class, EmailAuthMailSender.class})
public class EmailAuthMailSenderRejectionTest {

    @Autowired
    private EmailAuthMailSender emailAuthMailSender;

    @MockitoBean
    private MailUtil mailUtil;

    @MockitoBean
    private AuthRedisUtil authRedisUtil;

    @Test
    @DisplayName("[실패] 메일 발송 스레드와 큐가 모두 차면 비동기 발송 요청에서 거절 예외가 발생한다.")
    void 메일_발송_스레드와_큐가_모두_차면_비동기_발송_요청에서_거절_예외가_발생한다() {
        final CountDownLatch blockLatch = new CountDownLatch(1);
        willAnswer(invocation -> {
            blockLatch.await();
            return null;
        }).given(mailUtil).sendMail(any(), any(), any());

        try {
            for (int i = 0; i < 505; i++) {
                emailAuthMailSender.send("rejection@pusan.ac.kr", "code", "authKey", "countKey");
            }

            assertThatThrownBy(() -> emailAuthMailSender.send("rejection@pusan.ac.kr", "code", "authKey", "countKey"))
                    .isInstanceOf(TaskRejectedException.class);
        } finally {
            blockLatch.countDown();
        }
    }
}
