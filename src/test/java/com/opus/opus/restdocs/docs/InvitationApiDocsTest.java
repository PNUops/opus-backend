package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_INVITED;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVALID_INVITATION_ROLE;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.NOT_FOUND_INVITATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.invitation.application.dto.request.InvitationCreateRequest;
import com.opus.opus.modules.invitation.application.dto.response.InvitationCreateResponse;
import com.opus.opus.modules.invitation.application.dto.response.InvitationResponse;
import com.opus.opus.modules.invitation.exception.InvitationException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class InvitationApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";
    private static final String INVITATION_TOKEN = "Qw3rTy-invitation-token";

    @BeforeEach
    void setUp() {
        final Member member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 관리자가 외부멘토 초대 링크를 발급하면 201을 반환한다.")
    void 관리자가_외부멘토_초대_링크를_발급하면_201을_반환한다() throws Exception {
        final InvitationCreateRequest request = createRequest(MemberRoleType.ROLE_외부멘토);
        final InvitationCreateResponse response = new InvitationCreateResponse(
                1L, INVITATION_TOKEN, LocalDateTime.of(2026, 11, 21, 18, 0));

        when(invitationCommandService.createInvitation(any(), any())).thenReturn(response);

        mockMvc.perform(post("/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-invitation",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                numberFieldWithPath("contestId", "초대할 대회 ID"),
                                stringFieldWithPath("name", "초대받는 사람 이름"),
                                stringFieldWithPath("email", "초대받는 사람 이메일"),
                                stringFieldWithPath("role", "초대 역할 (MemberRoleType: ROLE_외부멘토만 가능)")
                        ),
                        responseFields(
                                numberFieldWithPath("invitationId", "초대 ID"),
                                stringFieldWithPath("token", "초대 링크 토큰 (발급 응답에서만 원문 제공)"),
                                dateTimeFieldWithPath("expiresAt", "토큰 만료 시각 (발급 후 48시간)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 외부멘토가 아닌 역할로 초대 링크를 발급하면 400 에러를 반환한다.")
    void 외부멘토가_아닌_역할로_초대_링크를_발급하면_400_에러를_반환한다() throws Exception {
        final InvitationCreateRequest request = createRequest(MemberRoleType.ROLE_교수);

        when(invitationCommandService.createInvitation(any(), any()))
                .thenThrow(new InvitationException(INVALID_INVITATION_ROLE));

        mockMvc.perform(post("/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-invitation-fail-invalid-role",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 같은 대회에 취소되지 않은 초대가 있으면 409 에러를 반환한다.")
    void 같은_대회에_취소되지_않은_초대가_있으면_409_에러를_반환한다() throws Exception {
        final InvitationCreateRequest request = createRequest(MemberRoleType.ROLE_외부멘토);

        when(invitationCommandService.createInvitation(any(), any()))
                .thenThrow(new InvitationException(ALREADY_INVITED));

        mockMvc.perform(post("/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(document("create-invitation-fail-already-invited",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회로 초대 링크를 발급하면 404 에러를 반환한다.")
    void 존재하지_않는_대회로_초대_링크를_발급하면_404_에러를_반환한다() throws Exception {
        final InvitationCreateRequest request = createRequest(MemberRoleType.ROLE_외부멘토);

        when(invitationCommandService.createInvitation(any(), any()))
                .thenThrow(new ContestException(NOT_FOUND_CONTEST));

        mockMvc.perform(post("/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("create-invitation-fail-contest-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이메일 형식이 올바르지 않으면 400 에러를 반환한다.")
    void 이메일_형식이_올바르지_않으면_400_에러를_반환한다() throws Exception {
        final InvitationCreateRequest request = new InvitationCreateRequest(
                1L, "김멘토", "invalid-email", MemberRoleType.ROLE_외부멘토);

        mockMvc.perform(post("/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-invitation-fail-invalid-email",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 초대 토큰으로 초대 정보를 조회하면 200을 반환한다.")
    void 초대_토큰으로_초대_정보를_조회하면_200을_반환한다() throws Exception {
        final InvitationResponse response = new InvitationResponse(
                "2026 SW 프로젝트 경진대회",
                MemberRoleType.ROLE_외부멘토,
                "mentor@example.com",
                "PENDING",
                LocalDateTime.of(2026, 11, 21, 18, 0),
                LocalDateTime.of(2026, 11, 19, 18, 0));

        when(invitationQueryService.getInvitation(any())).thenReturn(response);

        mockMvc.perform(get("/invitations/{token}", INVITATION_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-invitation",
                        pathParameters(
                                parameterWithName("token").description("초대 링크 토큰")
                        ),
                        responseFields(
                                stringFieldWithPath("contestName", "대회 이름"),
                                stringFieldWithPath("role", "초대 역할 (MemberRoleType)"),
                                stringFieldWithPath("email", "초대받은 이메일"),
                                stringFieldWithPath("status",
                                        "초대 상태 (PENDING: 수락 가능, ACCEPTED: 수락 완료, CANCELED: 취소, "
                                                + "EXPIRED: 만료, REPLACED: 재발급으로 교체)"),
                                dateTimeFieldWithPath("expiresAt", "토큰 만료 시각"),
                                dateTimeFieldWithPath("serverTime", "서버 현재 시각")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 초대 토큰으로 조회하면 404 에러를 반환한다.")
    void 존재하지_않는_초대_토큰으로_조회하면_404_에러를_반환한다() throws Exception {
        when(invitationQueryService.getInvitation(any()))
                .thenThrow(new InvitationException(NOT_FOUND_INVITATION));

        mockMvc.perform(get("/invitations/{token}", "unknown-token"))
                .andExpect(status().isNotFound())
                .andDo(document("get-invitation-fail-not-found",
                        pathParameters(
                                parameterWithName("token").description("존재하지 않는 초대 링크 토큰")
                        )
                ));
    }

    private InvitationCreateRequest createRequest(final MemberRoleType role) {
        return new InvitationCreateRequest(1L, "김멘토", "mentor@example.com", role);
    }
}
