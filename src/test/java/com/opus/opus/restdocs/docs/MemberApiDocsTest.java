package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_UPDATE_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.PasswordUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.request.StudentIdUpdateRequest;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.application.dto.response.StatisticsSummaryResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
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
        setField(member, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 회원가입 이메일 인증 코드는 발송된다.")
    void 유효한_요청이면_회원가입_이메일_인증_코드는_발송된다() throws Exception {
        final EmailAuthRequest request = new EmailAuthRequest(member.getEmail());

        doNothing().when(memberCommandService).signUpEmailAuth(any());

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
    @DisplayName("[실패] 부산대 도메인이 아닌 이메일로 인증 요청 시 400 에러를 반환한다.")
    void 부산대_도메인이_아닌_이메일로_인증_요청_시_에러를_반환한다() throws Exception {
        final EmailAuthRequest request = new EmailAuthRequest("test@gmail.com");

        willThrow(new MemberException(NOT_PUSAN_UNIVERSITY_EMAIL)).given(memberCommandService).signUpEmailAuth(any());

        mockMvc.perform(post("/sign-up/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("signup-auth-fail",
                        requestFields(
                                stringFieldWithPath("email", "잘못된 도메인의 이메일 (부산대 메일이 아님)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 정상적으로 회원가입 이메일 인증코드를 확인할 수 있다.")
    void 정상적으로_회원가입_이메일_인증코드를_확인할_수_있다() throws Exception {
        final EmailAuthConfirmRequest request = new EmailAuthConfirmRequest(member.getEmail(), "exampleCode");

        doNothing().when(memberCommandService).confirmSignUpEmailAuth(any());

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
    @DisplayName("[실패] 인증번호가 일치하지 않으면 400 에러를 반환한다.")
    void 인증번호가_일치하지_않으면_에러를_반환한다() throws Exception {
        final EmailAuthConfirmRequest request = new EmailAuthConfirmRequest(member.getEmail(), "wrong_code");

        willThrow(new MemberException(CANNOT_MATCH_EMAIL_AUTH_CODE)).given(memberCommandService)
                .confirmSignUpEmailAuth(any());

        mockMvc.perform(patch("/sign-up/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("signup-auth-confirm-fail",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일"),
                                stringFieldWithPath("authCode", "틀린 인증 코드")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 인증번호가 만료되면 400 에러를 반환한다.")
    void 인증번호가_만료되면_않으면_에러를_반환한다() throws Exception {
        final EmailAuthConfirmRequest request = new EmailAuthConfirmRequest(member.getEmail(), "expired_code");

        willThrow(new MemberException(CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE)).given(memberCommandService)
                .confirmSignUpEmailAuth(any());

        mockMvc.perform(patch("/sign-up/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("signup-auth-confirm-fail2",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일"),
                                stringFieldWithPath("authCode", "만료된 인증 코드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 회원가입은 정상적으로 이뤄진다.")
    void 유효한_요청이면_회원가입은_정상적으로_이뤄진다() throws Exception {
        final SignUpRequest request = new SignUpRequest(member.getName(), member.getStudentId(), member.getEmail(),
                "qwer123!");

        doNothing().when(memberCommandService).signUp(any());

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

    @Test
    @DisplayName("[성공] 유효한 요청이면 로그인은 정상적으로 이뤄진다.")
    void 유효한_요청이면_로그인은_정상적으로_이뤄진다() throws Exception {
        final SignInResponse response = new SignInResponse(member.getId(), member.getName(), "exampleToken",
                member.getRoles());
        final SignInRequest request = new SignInRequest(member.getEmail(), "qwer123!");

        when(memberCommandService.signIn(any())).thenReturn(response);

        mockMvc.perform(post("/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("signin",
                        requestFields(
                                stringFieldWithPath("email", "로그인 이메일"),
                                stringFieldWithPath("password", "비밀번호 (영문+숫자+특수문자 조합)")
                        ),
                        responseFields(
                                numberFieldWithPath("memberId", "회원 고유 식별자"),
                                stringFieldWithPath("name", "회원 이름"),
                                stringFieldWithPath("token", "JWT 액세스 토큰"),
                                arrayFieldWithPath("types", "회원 권한 목록(회원, 관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 비밀번호 변경 이메일 인증 코드는 발송된다.")
    void 유효한_요청이면_비밀번호_변경_이메일_인증_코드는_발송된다() throws Exception {
        final EmailAuthRequest request = new EmailAuthRequest(member.getEmail());

        doNothing().when(memberCommandService).signInEmailAuth(any());

        mockMvc.perform(post("/sign-in/password-reset/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("signin-auth",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 정상적으로 비밀번호 변경 이메일 인증코드를 확인할 수 있다.")
    void 정상적으로_비밀번호_변경_인증코드를_확인할_수_있다() throws Exception {
        final EmailAuthConfirmRequest request = new EmailAuthConfirmRequest(member.getEmail(), "exampleCode");

        doNothing().when(memberCommandService).confirmSignInEmailAuth(any());

        mockMvc.perform(patch("/sign-in/password-reset/email-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("signin-auth-confirm",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일"),
                                stringFieldWithPath("authCode", "인증 코드")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 비밀번호는 변경된다.")
    void 유효한_요청이면_정상적으로_비밀번호는_변경된다() throws Exception {
        final PasswordUpdateRequest request = new PasswordUpdateRequest(member.getEmail(), "newPassword1!");

        doNothing().when(memberCommandService).updatePassword(request);

        mockMvc.perform(patch("/sign-in/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-password",
                        requestFields(
                                stringFieldWithPath("email", "가입 이메일"),
                                stringFieldWithPath("newPassword", "새로운 비밀번호")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 회원이라면 정상적으로 가입 이메일을 찾을 수 있다.")
    void 회원이라면_정상적으로_가입_이메일을_찾을_수_있다() throws Exception {
        final EmailFindResponse response = new EmailFindResponse(member.getEmail());

        when(memberQueryService.getMyEmail(any())).thenReturn(response);

        mockMvc.perform(get("/sign-in/{studentId}/email-find", 1))
                .andExpect(status().isOk())
                .andDo(document("get-password",
                        pathParameters(
                                parameterWithName("studentId").description("가입 학번")
                        ),
                        responseFields(
                                stringFieldWithPath("email", "가입된 이메일")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 로컬 환경 리다이렉트 쿠키가 정상적으로 설정된다.")
    void 로컬_환경_리다이렉트_쿠키가_정상적으로_설정된다() throws Exception {
        mockMvc.perform(post("/oauth2/set-redirect"))
                .andExpect(status().isNoContent())
                .andDo(document("oauth2-set-redirect"));
    }

    @Test
    @DisplayName("[성공] 학번 수정이 정상적으로 이루어진다.")
    void 학번_수정이_정상적으로_이루어진다() throws Exception {
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");

        doNothing().when(memberCommandService).updateStudentId(any(), any());

        mockMvc.perform(patch("/members/me/student-id")
                        .header("Authorization", "Bearer exampleToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-student-id",
                        requestFields(
                                stringFieldWithPath("studentId", "변경할 학번")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 학번 수정이 불가능한 사용자면 400 에러를 반환한다.")
    void 학번_수정이_불가능한_사용자면_에러를_반환한다() throws Exception {
        final StudentIdUpdateRequest request = new StudentIdUpdateRequest("202512345");

        willThrow(new MemberException(CANNOT_UPDATE_STUDENT_ID))
                .given(memberCommandService).updateStudentId(any(), any());

        mockMvc.perform(patch("/members/me/student-id")
                        .header("Authorization", "Bearer exampleToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-student-id-fail",
                        requestFields(
                                stringFieldWithPath("studentId", "변경할 학번")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 메인 페이지 통계 요약을 정상적으로 조회할 수 있다.")
    void 메인_페이지_통계_요약을_정상적으로_조회할_수_있다() throws Exception {
        final StatisticsSummaryResponse response = new StatisticsSummaryResponse(42L, 128L, 5L);

        when(statisticsQueryService.getStatisticsSummary()).thenReturn(response);

        mockMvc.perform(get("/statistics/summary"))
                .andExpect(status().isOk())
                .andDo(document("statistics-summary",
                        responseFields(
                                numberFieldWithPath("totalProjects", "등록된 프로젝트 수"),
                                numberFieldWithPath("totalLikes", "총 좋아요 수"),
                                numberFieldWithPath("totalContests", "진행된 대회 수")
                        )
                ));
    }
}
