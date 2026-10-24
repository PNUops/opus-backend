package com.opus.opus.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.member.application.EmailAuthMailSender;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;

public class EmailAuthMailSenderTest extends IntegrationTest {

    private static final String EMAIL = "mail-sender@pusan.ac.kr";
    private static final String AUTH_KEY = "signup:email:auth:" + EMAIL;
    private static final String COUNT_KEY = "email:auth:count:" + EMAIL;

    @Autowired
    private EmailAuthMailSender emailAuthMailSender;

    @BeforeEach
    void setUp() {
        authRedisUtil.delete(AUTH_KEY);
        authRedisUtil.delete(COUNT_KEY);
    }

    @Test
    @DisplayName("[성공] 메일 발송에 성공하면 인증 코드를 10분 TTL로 저장한다.")
    void 메일_발송에_성공하면_인증_코드를_10분_TTL로_저장한다() {
        emailAuthMailSender.send(EMAIL, "newCode", AUTH_KEY, COUNT_KEY);

        assertThat(authRedisUtil.get(AUTH_KEY)).isEqualTo("newCode");
        assertThat(authRedisUtil.ttl(AUTH_KEY, TimeUnit.SECONDS)).isBetween(590L, 600L);
    }

    @Test
    @DisplayName("[실패] 메일 발송에 실패하면 인증 코드를 저장하지 않는다.")
    void 메일_발송에_실패하면_인증_코드를_저장하지_않는다() {
        willThrow(new MailSendException("SMTP 응답 없음")).given(mailUtil).sendMail(any(), any(), any());

        emailAuthMailSender.send(EMAIL, "newCode", AUTH_KEY, COUNT_KEY);

        assertThat(authRedisUtil.exists(AUTH_KEY)).isFalse();
    }

    @Test
    @DisplayName("[실패] 재전송 메일 발송에 실패하면 이전 인증 코드는 유지된다.")
    void 재전송_메일_발송에_실패하면_이전_인증_코드는_유지된다() {
        authRedisUtil.set(AUTH_KEY, "previousCode", 10, TimeUnit.MINUTES);
        willThrow(new MailSendException("SMTP 응답 없음")).given(mailUtil).sendMail(any(), any(), any());

        emailAuthMailSender.send(EMAIL, "newCode", AUTH_KEY, COUNT_KEY);

        assertThat(authRedisUtil.get(AUTH_KEY)).isEqualTo("previousCode");
    }

    @Test
    @DisplayName("[실패] 메일 발송에 실패하면 인증 요청 횟수를 되돌린다.")
    void 메일_발송에_실패하면_인증_요청_횟수를_되돌린다() {
        authRedisUtil.incrementWithTtl(COUNT_KEY, 1, TimeUnit.DAYS);
        authRedisUtil.incrementWithTtl(COUNT_KEY, 1, TimeUnit.DAYS);
        willThrow(new MailSendException("SMTP 응답 없음")).given(mailUtil).sendMail(any(), any(), any());

        emailAuthMailSender.send(EMAIL, "newCode", AUTH_KEY, COUNT_KEY);

        assertThat(authRedisUtil.get(COUNT_KEY)).isEqualTo("1");
    }
}
