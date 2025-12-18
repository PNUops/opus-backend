package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class MemberApiDocsTest extends RestDocsTest {

    private Member member;

    @BeforeEach
    void setUp() {
        this.member = MemberFixture.createMember();
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 회원가입 이메일 인증 코드는 발송된다.")
    void 유효한_요청이면_회원가입_이메일_인증_코드는_발송된다() throws Exception {
        doNothing().when(memberCommandService).signUpEmailAuth(any());

        final EmailAuthRequest request = new EmailAuthRequest(member.getEmail());

        mockMvc.perform(post("/sign-up/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("signup-auth",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 정상적으로 이메일 인증코드를 확인할 수 있다.")
    void 정상적으로_이메일_인증코드를_확인할_수_있다() throws Exception {
        doNothing().when(memberCommandService).confirmSignUpEmailAuth(any());

        final EmailAuthConfirmRequest request = new EmailAuthConfirmRequest(member.getEmail(), "exampleCode");

        mockMvc.perform(patch("/sign-up/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("signup-auth-confirm",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일"),
                                stringFieldWithPath("authCode", "인증 코드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 회원가입은 정상적으로 이뤄진다.")
    void 유효한_요청이면_회원가입은_정상적으로_이뤄진다() throws Exception {
        doNothing().when(memberCommandService).signUp(any());

        final SignUpRequest request = new SignUpRequest(member.getName(), member.getStudentId(), member.getEmail(),
                "qwer123!");
        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("signup",
                        requestFields(
                                stringFieldWithPath("name", "회원 이름"),
                                stringFieldWithPath("studentId", "회원의 학번"),
                                stringFieldWithPath("email", "회원의 이메일"),
                                stringFieldWithPath("password", "회원의 비밀번호")
                        )
                ));
    }
}
