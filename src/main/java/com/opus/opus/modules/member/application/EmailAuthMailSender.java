package com.opus.opus.modules.member.application;

import com.opus.opus.global.util.AuthRedisUtil;
import com.opus.opus.global.util.MailUtil;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAuthMailSender {

    private static final long AUTH_CODE_TTL = 10L;
    private static final String AUTH_MAIL_SUBJECT = "SW프로젝트관리시스템 인증코드 발송 메일입니다.";

    private final MailUtil mailUtil;
    private final AuthRedisUtil authRedisUtil;

    @Async("mailTaskExecutor")
    public void send(final String email, final String authCode, final String authKey, final String countKey,
                     final long requestedAt) {
        final long startTime = System.currentTimeMillis();
        try {
            sendAuthCodeMail(email, authCode);
            log.info("인증 메일 발송 완료 [email={}, queueWaitMs={}, sendMs={}]",
                    email, startTime - requestedAt, System.currentTimeMillis() - startTime);
            authRedisUtil.set(authKey, authCode, AUTH_CODE_TTL, TimeUnit.MINUTES);
        } catch (final MailException e) {
            log.warn("인증 메일 발송 실패 [email={}, queueWaitMs={}, sendMs={}]: {}",
                    email, startTime - requestedAt, System.currentTimeMillis() - startTime, e.getMessage());
            authRedisUtil.decrementIfExists(countKey);
        }
    }

    private void sendAuthCodeMail(final String email, final String authCode) {
        mailUtil.sendMail(List.of(email), AUTH_MAIL_SUBJECT, "인증코드는 " + authCode + " 입니다.");
    }
}
