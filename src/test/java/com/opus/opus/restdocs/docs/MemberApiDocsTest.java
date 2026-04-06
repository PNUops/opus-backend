package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_MATCH_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_UPDATE_STUDENT_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.CANNOT_VERIFY_EXPIRED_EMAIL_AUTH_CODE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_PUSAN_UNIVERSITY_EMAIL;
import static java.time.LocalDateTime.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.data.domain.Sort.Direction.DESC;
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
import com.opus.opus.modules.member.application.dto.request.GithubUrlUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.PasswordUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.SignInRequest;
import com.opus.opus.modules.member.application.dto.request.SignUpRequest;
import com.opus.opus.modules.member.application.dto.request.ProfileVisibilityUpdateRequest;
import com.opus.opus.modules.member.application.dto.request.StudentIdUpdateRequest;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse.CommentInfo;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse.ProjectInfo;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.domain.dao.MyVoteResponse;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.application.dto.response.StatisticsSummaryResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse.AwardInfo;
import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class MemberApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";

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

    @Test
    @DisplayName("[성공] 회원 탈퇴가 정상적으로 이루어진다.")
    void 회원_탈퇴가_정상적으로_이루어진다() throws Exception {
        // Given
        doNothing().when(memberCommandService).withdraw(any());

        // When & Then
        mockMvc.perform(delete("/members/me")
                        .header(HttpHeaders.AUTHORIZATION, memberAccessToken))
                .andExpect(status().isNoContent())
                .andDo(document("withdraw-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(회원)"))
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 관리자가 회원을 강제 탈퇴시킨다.")
    void 관리자가_회원을_강제_탈퇴시킨다() throws Exception {
        // Given
        doNothing().when(memberCommandService).withdrawByAdmin(any());

        // When & Then
        mockMvc.perform(delete("/admin/members/{memberId}", 1L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin.access.token"))
                .andExpect(status().isNoContent())
                .andDo(document("admin-withdraw-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(관리자)"))
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("탈퇴시킬 회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 회원을 강제 탈퇴하면 404를 반환한다.")
    void 존재하지_않는_회원을_강제_탈퇴하면_에러를_반환한다() throws Exception {
        // Given
        willThrow(new MemberException(NOT_FOUND_MEMBER))
                .given(memberCommandService).withdrawByAdmin(any());

        // When & Then
        mockMvc.perform(delete("/admin/members/{memberId}", 999L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin.access.token"))
                .andExpect(status().isNotFound())
                .andDo(document("admin-withdraw-member-fail-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description(
                                        String.format(authorizationHeaderDescription, "(관리자)"))
                        ),
                        pathParameters(
                                parameterWithName("memberId").description("존재하지 않는 회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 나의 프로젝트 목록을 조회할 수 있다.")
    void 나의_프로젝트_목록을_조회할_수_있다() throws Exception {
        final List<MyProjectResponse> responses = List.of(
                new MyProjectResponse(
                        1L, "제6회창의융합해커톤대회", 10L, "PNUops", "SW성과관리시스템", "AI/빅데이터",
                        List.of(new AwardInfo("대상", "#FF0000"))
                ),
                new MyProjectResponse(
                        2L, "제5회창의융합해커톤대회", 22L, "해커톤의신", "PNUDataKing", null,
                        List.of()
                )
        );

        when(memberQueryService.getMyProjects(any())).thenReturn(responses);

        mockMvc.perform(get("/members/me/projects")
                        .header("Authorization", "Bearer exampleToken"))
                .andExpect(status().isOk())
                .andDo(document("get-my-projects",
                        responseFields(
                                numberFieldWithPath("[].contestId", "대회 ID"),
                                stringFieldWithPath("[].contestName", "대회명"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                stringFieldWithPath("[].trackName", "트랙(분과)명").optional(),
                                arrayFieldWithPath("[].awards", "수상 내역 리스트"),
                                stringFieldWithPath("[].awards[].awardName", "수상명"),
                                stringFieldWithPath("[].awards[].awardColor", "수상 색상")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 나의 투표 기록을 조회할 수 있다.")
    void 나의_투표_기록을_조회할_수_있다() throws Exception {
        final List<MyVoteResponse> responses = List.of(
                new MyVoteResponse(1L, "제6회창의융합해커톤대회", 5L, "Team Alpha", "알파 프로젝트"),
                new MyVoteResponse(1L, "제6회창의융합해커톤대회", 12L, "Team Beta", "베타 프로젝트")
        );

        when(memberQueryService.getMyVotes(any())).thenReturn(responses);

        mockMvc.perform(get("/members/me/votes")
                        .header("Authorization", "Bearer exampleToken"))
                .andExpect(status().isOk())
                .andDo(document("get-my-votes",
                        responseFields(
                                numberFieldWithPath("[].contestId", "대회 ID"),
                                stringFieldWithPath("[].contestName", "대회명"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 로그인한 사용자의 계정 정보를 조회할 수 있다.")
    void 로그인한_사용자의_계정_정보를_조회할_수_있다() throws Exception {
        final AccountInfoResponse response = new AccountInfoResponse(member.getName(), member.getEmail(), null, true);

        when(memberQueryService.getAccountInfo(any())).thenReturn(response);

        mockMvc.perform(get("/members/me")
                        .header("Authorization", "Bearer exampleToken"))
                .andExpect(status().isOk())
                .andDo(document("get-account-info",
                        responseFields(
                                stringFieldWithPath("name", "이름"),
                                stringFieldWithPath("email", "이메일"),
                                stringFieldWithPath("githubUrl", "GitHub 링크").optional(),
                                booleanFieldWithPath("isProfilePublic", "프로필 공개 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] GitHub 링크를 정상적으로 수정할 수 있다.")
    void GitHub_링크를_정상적으로_수정할_수_있다() throws Exception {
        final GithubUrlUpdateRequest request = new GithubUrlUpdateRequest("https://github.com/hongjiyeon");

        doNothing().when(memberCommandService).updateGithubUrl(any(), any());

        mockMvc.perform(patch("/members/me/github-url")
                        .header("Authorization", "Bearer exampleToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-github-url",
                        requestFields(
                                stringFieldWithPath("githubUrl", "GitHub URL").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 프로필 공개 여부를 정상적으로 변경할 수 있다.")
    void 프로필_공개_여부를_정상적으로_변경할_수_있다() throws Exception {
        final ProfileVisibilityUpdateRequest request = new ProfileVisibilityUpdateRequest(true);

        doNothing().when(memberCommandService).updateProfileVisibility(any(), any());

        mockMvc.perform(patch("/members/me/profile-visibility")
                        .header("Authorization", "Bearer exampleToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-profile-visibility",
                        requestFields(
                                booleanFieldWithPath("isProfilePublic", "프로필 공개 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 나의 댓글 목록을 조회할 수 있다.")
    void 나의_댓글_목록을_조회할_수_있다() throws Exception {
        final List<MyCommentResponse> content = List.of(
                new MyCommentResponse(
                        new CommentInfo(1L, "인정합니다.", of(2026, 1, 1, 0, 0), "홍지연"),
                        new ProjectInfo(1L, "제6회창의융합해커톤대회", "해커톤", "창업트랙", 5L, "TeamName", "Project Name", "Artify는 일상 속 모든 순간을 예술로 재해석하는 크리에이티브 플랫폼입니다.")
                )
        );
        final Page<MyCommentResponse> page = new PageImpl<>(content,
                PageRequest.of(0, 10, Sort.by(DESC, "createdAt")), 1);

        when(memberQueryService.getMyComments(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/members/me/comments")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .param("sort", "latest")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andDo(document("get-my-comments",
                        queryParameters(
                                parameterWithName("sort").description("정렬 기준 (latest, oldest)").optional(),
                                parameterWithName("startDate").description("조회 시작일 (yyyy-MM-dd)").optional(),
                                parameterWithName("endDate").description("조회 종료일 (yyyy-MM-dd)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("content[]", "댓글 목록"),
                                numberFieldWithPath("content[].comment.commentId", "댓글 ID"),
                                stringFieldWithPath("content[].comment.content", "댓글 내용"),
                                dateTimeFieldWithPath("content[].comment.createdAt", "작성 일시"),
                                stringFieldWithPath("content[].comment.memberName", "작성자 이름"),
                                numberFieldWithPath("content[].project.contestId", "대회 ID"),
                                stringFieldWithPath("content[].project.contestName", "대회명"),
                                stringFieldWithPath("content[].project.categoryName", "카테고리명"),
                                stringFieldWithPath("content[].project.trackName", "트랙명"),
                                numberFieldWithPath("content[].project.teamId", "팀 ID"),
                                stringFieldWithPath("content[].project.teamName", "팀명"),
                                stringFieldWithPath("content[].project.projectName", "프로젝트명"),
                                stringFieldWithPath("content[].project.overview", "프로젝트 설명 (최대 100자)"),
                                subsectionFieldWithPath("pageable", "페이지 정보"),
                                booleanFieldWithPath("last", "마지막 페이지 여부"),
                                numberFieldWithPath("totalPages", "전체 페이지 수"),
                                numberFieldWithPath("totalElements", "전체 요소 수"),
                                booleanFieldWithPath("first", "첫 페이지 여부"),
                                numberFieldWithPath("size", "페이지 크기"),
                                numberFieldWithPath("number", "현재 페이지 번호"),
                                subsectionFieldWithPath("sort", "정렬 정보"),
                                numberFieldWithPath("numberOfElements", "현재 페이지 요소 수"),
                                booleanFieldWithPath("empty", "비어있는 페이지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 날짜 범위가 불완전하면 나의 댓글 조회 시 400 에러를 반환한다.")
    void 날짜_범위가_불완전하면_나의_댓글_조회_시_에러를_반환한다() throws Exception {
        willThrow(new MemberException(INVALID_DATE_RANGE))
                .given(memberQueryService).getMyComments(any(), any(), any(), any(), anyInt(), anyInt());

        mockMvc.perform(get("/members/me/comments")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .param("startDate", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andDo(document("get-my-comments-fail-date-range"));
    }

    @Test
    @DisplayName("[성공] 최근 좋아요한 프로젝트 미리보기를 조회할 수 있다.")
    void 최근_좋아요한_프로젝트_미리보기를_조회할_수_있다() throws Exception {
        final List<MyLikePreviewResponse> responses = List.of(
                new MyLikePreviewResponse(3L, "Team Gamma", 1L, "감마 프로젝트", "제6회창의융합해커톤대회"),
                new MyLikePreviewResponse(7L, "Team Delta", 2L, "델타 프로젝트", "제5회창의융합해커톤대회"),
                new MyLikePreviewResponse(15L, "Team Epsilon", 2L, "엡실론 프로젝트", "제5회창의융합해커톤대회")
        );

        when(memberQueryService.getMyLikePreview(any())).thenReturn(responses);

        mockMvc.perform(get("/members/me/likes/preview")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-my-like-preview",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "좋아요 미리보기 목록 (최대 3개)"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                numberFieldWithPath("[].contestId", "대회 ID"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                stringFieldWithPath("[].contestName", "대회명")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 나의 좋아요 전체 목록을 조회할 수 있다.")
    void 나의_좋아요_전체_목록을_조회할_수_있다() throws Exception {
        final List<MyLikedProjectResponse> content = List.of(
                new MyLikedProjectResponse(3L, "Team Gamma", "감마 프로젝트", 1L, "제6회창의융합해커톤대회"),
                new MyLikedProjectResponse(7L, "Team Delta", "델타 프로젝트", 2L, "제5회창의융합해커톤대회")
        );
        final Page<MyLikedProjectResponse> page = new PageImpl<>(content,
                PageRequest.of(0, 12, Sort.by(DESC, "createdAt")), 2);

        when(memberQueryService.getMyLikedProjects(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/members/me/likes")
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .param("sort", "latest")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andDo(document("get-my-liked-projects",
                        queryParameters(
                                parameterWithName("sort").description("정렬 기준 (latest, oldest)").optional(),
                                parameterWithName("startDate").description("조회 시작일 (yyyy-MM-dd)").optional(),
                                parameterWithName("endDate").description("조회 종료일 (yyyy-MM-dd)").optional(),
                                parameterWithName("categoryId").description("대회 카테고리 필터").optional(),
                                parameterWithName("contestId").description("특정 대회 필터").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("content[]", "좋아요 프로젝트 목록"),
                                numberFieldWithPath("content[].teamId", "팀 ID"),
                                stringFieldWithPath("content[].teamName", "팀명"),
                                stringFieldWithPath("content[].projectName", "프로젝트명"),
                                numberFieldWithPath("content[].contestId", "대회 ID"),
                                stringFieldWithPath("content[].contestName", "대회명"),
                                subsectionFieldWithPath("pageable", "페이지 정보"),
                                booleanFieldWithPath("last", "마지막 페이지 여부"),
                                numberFieldWithPath("totalPages", "전체 페이지 수"),
                                numberFieldWithPath("totalElements", "전체 요소 수"),
                                booleanFieldWithPath("first", "첫 페이지 여부"),
                                numberFieldWithPath("size", "페이지 크기"),
                                numberFieldWithPath("number", "현재 페이지 번호"),
                                subsectionFieldWithPath("sort", "정렬 정보"),
                                numberFieldWithPath("numberOfElements", "현재 페이지 요소 수"),
                                booleanFieldWithPath("empty", "비어있는 페이지 여부")
                        )
                ));
    }
}
