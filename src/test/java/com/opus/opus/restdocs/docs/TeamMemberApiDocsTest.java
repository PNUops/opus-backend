package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.member.exception.MemberExceptionType.MISMATCH_STUDENT_ID_AND_NAME;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_ALREADY_EXISTS;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.application.dto.request.TeamMemberCreateRequest;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TeamMemberApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";
    private Member admin;

    @BeforeEach
    void setUp() {
        admin = MemberFixture.createMember();
        setField(admin, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any(MethodParameter.class)))
                .thenReturn(true);
        when(memberArgumentResolver.resolveArgument(
                any(MethodParameter.class),
                any(ModelAndViewContainer.class),
                any(NativeWebRequest.class),
                any(WebDataBinderFactory.class)
        )).thenReturn(admin);
    }

    // 팀원 추가

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 팀원이 추가된다.")
    void 유효한_요청이면_정상적으로_팀원이_추가된다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("이옵스", "202612345", ROLE_팀원);

        doNothing().when(teamMemberCommandService).createTeamMember(any(), any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/members", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("add-team-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "추가할 팀원 이름"),
                                stringFieldWithPath("memberStudentId", "추가할 팀원 학번"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀원명이 비어있으면 400 에러를 반환한다.")
    void 팀원명이_비어있으면_에러를_반환한다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("", "202612345", ROLE_팀원);

        mockMvc.perform(post("/teams/{teamId}/members", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("add-team-member-fail-empty-name",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "비어있는 팀원명"),
                                stringFieldWithPath("memberStudentId", "팀원 학번"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀원학번이 비어있으면 400 에러를 반환한다.")
    void 팀원학번이_비어있으면_에러를_반환한다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("이옵스", "", ROLE_팀원);

        mockMvc.perform(post("/teams/{teamId}/members", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("add-team-member-fail-empty-student-id",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "팀원명"),
                                stringFieldWithPath("memberStudentId", "비어있는 팀원 학번"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀 ID인 경우 404 에러를 반환한다.")
    void 존재하지_않는_팀_ID인_경우_에러를_반환한다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("이옵스", "202612345", ROLE_팀원);

        willThrow(new TeamException(NOT_FOUND_TEAM)).given(teamMemberCommandService)
                .createTeamMember(any(), any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/members", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("add-team-member-fail-team-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "팀원명"),
                                stringFieldWithPath("memberStudentId", "팀원 학번"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀원명과 팀원학번이 맞지 않으면 400 에러를 반환한다.")
    void 팀원명과_팀원학번이_맞지_않으면_에러를_반환한다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("이옵스", "202612345", ROLE_팀원);

        willThrow(new MemberException(MISMATCH_STUDENT_ID_AND_NAME)).given(teamMemberCommandService)
                .createTeamMember(any(), any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/members", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("add-team-member-fail-mismatch",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "팀원명 (학번과 일치하지 않음)"),
                                stringFieldWithPath("memberStudentId", "팀원 학번 (이름과 일치하지 않음)"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 동일한 참가자명 + 학번이 해당 팀에 있는 경우 409 에러를 반환한다.")
    void 동일한_참가자명과_학번이_해당_팀에_있는_경우_에러를_반환한다() throws Exception {
        final TeamMemberCreateRequest request = new TeamMemberCreateRequest("이옵스", "202612345", ROLE_팀원);

        willThrow(new TeamMemberException(TEAM_MEMBER_ALREADY_EXISTS)).given(teamMemberCommandService)
                .createTeamMember(any(), any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/members", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isConflict())
                .andDo(document("add-team-member-fail-already-exists",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestFields(
                                stringFieldWithPath("memberName", "이미 등록된 팀원명"),
                                stringFieldWithPath("memberStudentId", "이미 등록된 팀원 학번"),
                                stringFieldWithPath("roleType", "추가할 팀원의 역할(ROLE_팀장, ROLE_팀원)")
                        )
                ));
    }

    // 팀원 삭제

    @Test
    @DisplayName("[성공] 유효한 요청이면 정상적으로 팀원이 삭제된다.")
    void 유효한_요청이면_정상적으로_팀원이_삭제된다() throws Exception {
        doNothing().when(teamMemberCommandService).deleteTeamMember(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/members/{memberId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-member",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("memberId").description("회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀 ID인 경우 404 에러를 반환한다.")
    void 삭제_존재하지_않는_팀_ID인_경우_에러를_반환한다() throws Exception {
        willThrow(new TeamException(NOT_FOUND_TEAM)).given(teamMemberCommandService)
                .deleteTeamMember(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/members/{memberId}", 999, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("delete-team-member-fail-team-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID"),
                                parameterWithName("memberId").description("회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 멤버 ID인 경우 404 에러를 반환한다.")
    void 존재하지_않는_멤버_ID인_경우_에러를_반환한다() throws Exception {
        willThrow(new MemberException(NOT_FOUND_MEMBER)).given(teamMemberCommandService)
                .deleteTeamMember(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/members/{memberId}", 1, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("delete-team-member-fail-member-not-found",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("memberId").description("존재하지 않는 회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 삭제 대상 팀원이 해당 팀에 없을 경우 404 에러를 반환한다.")
    void 삭제_대상_팀원이_해당_팀에_없을_경우_에러를_반환한다() throws Exception {
        willThrow(new TeamMemberException(TEAM_MEMBER_NOT_FOUND_IN_TEAM))
                .given(teamMemberCommandService)
                .deleteTeamMember(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/members/{memberId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("delete-team-member-fail-not-in-team",
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자 또는 팀장)")
                        ),
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("memberId").description("해당 팀에 속하지 않은 회원 ID")
                        )
                ));
    }
}
