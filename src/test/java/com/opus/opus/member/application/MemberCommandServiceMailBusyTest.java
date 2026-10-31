package com.opus.opus.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.EMAIL_AUTH_MAIL_BUSY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.member.application.EmailAuthMailSender;
import com.opus.opus.modules.member.application.MemberCommandService;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class MemberCommandServiceMailBusyTest extends IntegrationTest {

    private static final String EMAIL = "mail-busy@pusan.ac.kr";
    private static final String COUNT_KEY = "email:auth:count:" + EMAIL;

    @Autowired
    private MemberCommandService memberCommandService;

    @MockitoBean
    private EmailAuthMailSender emailAuthMailSender;

    @BeforeEach
    void setUp() {
        authRedisUtil.delete(COUNT_KEY);
        willThrow(new TaskRejectedException("메일 발송 큐 초과")).given(emailAuthMailSender)
                .send(any(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("[실패] 메일 발송 큐가 가득 차면 인증 메일을 요청할 수 없다.")
    void 메일_발송_큐가_가득_차면_인증_메일을_요청할_수_없다() {
        assertThatThrownBy(() -> memberCommandService.signUpEmailAuth(new EmailAuthRequest(EMAIL)))
                .isInstanceOf(MemberException.class)
                .hasMessage(EMAIL_AUTH_MAIL_BUSY.errorMessage());
    }

    @Test
    @DisplayName("[실패] 메일 발송 큐가 가득 차면 인증 요청 횟수를 되돌린다.")
    void 메일_발송_큐가_가득_차면_인증_요청_횟수를_되돌린다() {
        assertThatThrownBy(() -> memberCommandService.signUpEmailAuth(new EmailAuthRequest(EMAIL)))
                .isInstanceOf(MemberException.class);

        assertThat(authRedisUtil.get(COUNT_KEY)).isEqualTo("0");
    }
}
