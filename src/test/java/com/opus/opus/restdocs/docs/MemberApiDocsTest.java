package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_UPDATE_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.dto.request.EmailAuthConfirmRequest;
import com.opus.opus.modules.member.application.dto.request.EmailAuthRequest;
import com.opus.opus.modules.member.application.dto.request.PasswordUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.request.StudentIdUpdateRequest;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class MemberApiDocsTest extends RestDocsTest {

    private Member member;
    private String memberAccessToken;
    private String authorizationHeaderDescription;
    private byte[] testImage;

    @BeforeEach
    void setUp() {
        this.member = MemberFixture.createMember();
        setField(member, "id", 1L);
        this.memberAccessToken = "Bearer member.access.token";
        this.authorizationHeaderDescription = "Bearer %s.access.token";
        this.testImage = "test-image-content".getBytes();

        when(memberArgumentResolver.supportsParameter(
                argThat(param -> param != null && param.hasParameterAnnotation(LoginMember.class))))
                .thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
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
    @DisplayName("[성공] 프로필 이미지를 조회한다.")
    void 프로필_이미지를_조회한다() throws Exception {
        // Given
        final ImageResponse response = new ImageResponse(new ByteArrayResource(testImage), "image/png");

        when(memberQueryService.getProfileImage(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/members/me/images/profile")
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andDo(document("get-member-profile-image",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(회원)"))
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 프로필 이미지가 없으면 404를 반환한다.")
    void 프로필_이미지가_없으면_404를_반환한다() throws Exception {
        // Given
        when(memberQueryService.getProfileImage(any()))
                .thenThrow(new FileException(NOT_EXISTS_MATCHING_IMAGE_ID));

        // When & Then
        mockMvc.perform(get("/members/me/images/profile")
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isNotFound())
                .andDo(document("get-member-profile-image-fail-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(회원)"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 프로필 이미지를 변경한다.")
    void 프로필_이미지를_변경한다() throws Exception {
        // Given
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                testImage
        );

        doNothing().when(memberCommandService).modifyProfileImage(any(), any());

        // When & Then
        mockMvc.perform(multipart("/members/me/images/profile")
                        .file(image)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNoContent())
                .andDo(document("modify-member-profile-image",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(회원)"))
                        ),
                        requestParts(
                                partWithName("image").description("변경할 프로필 이미지 (모든 이미지 형식 지원)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 프로필 이미지를 삭제한다.")
    void 프로필_이미지를_삭제한다() throws Exception {
        // Given
        doNothing().when(memberCommandService).deleteProfileImage(any());

        // When & Then
        mockMvc.perform(delete("/members/me/images/profile")
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("delete-member-profile-image",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(회원)"))
                        )
                ));
    }
}
