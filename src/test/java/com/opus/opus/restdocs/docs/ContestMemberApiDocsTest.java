package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.application.dto.request.StaffTeamUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse.TeamInfo;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestMemberApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @Test
    @DisplayName("[성공] 배정된 교수/외부멘토 목록을 조회한다.")
    void 배정된_교수_외부멘토_목록을_조회한다() throws Exception {
        final List<ContestStaffResponse> responses = List.of(
                new ContestStaffResponse(1L, 2L, "홍길동", "gildong@example.com", "ROLE_교수",
                        List.of(new TeamInfo(10L, "개발 1팀"), new TeamInfo(12L, "운영 기획팀"))),
                new ContestStaffResponse(2L, 3L, "이멘토", "mentor@example.com", "ROLE_외부멘토",
                        List.of(new TeamInfo(10L, "개발 1팀")))
        );

        when(contestMemberQueryService.getAssignedStaff(any(), any(), any())).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/staff", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .param("memberType", "ROLE_교수")
                        .param("search", "개발"))
                .andExpect(status().isOk())
                .andDo(document("get-contest-member",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        queryParameters(
                                parameterWithName("memberType").optional()
                                        .description("회원 유형 필터 (MemberRoleType: ROLE_교수, ROLE_외부멘토)"),
                                parameterWithName("search").optional()
                                        .description("이름 또는 담당 팀 이름 검색어")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "배정 목록"),
                                numberFieldWithPath("[].contestMemberId", "배정 ID"),
                                numberFieldWithPath("[].memberId", "회원 ID"),
                                stringFieldWithPath("[].name", "이름"),
                                stringFieldWithPath("[].email", "이메일"),
                                stringFieldWithPath("[].roleType", "회원 유형 (MemberRoleType)"),
                                arrayFieldWithPath("[].teams", "담당 팀 목록"),
                                numberFieldWithPath("[].teams[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teams[].teamName", "팀 이름")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 교수/외부멘토를 담당 팀에 일괄 배정한다.")
    void 교수_외부멘토를_담당_팀에_일괄_배정한다() throws Exception {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(2L, 3L),
                List.of(10L, 12L));

        doNothing().when(contestMemberCommandService).assignStaff(any(), any());

        mockMvc.perform(post("/contests/{contestId}/staff/batch", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("assign-contest-member",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                arrayFieldWithPath("memberIds", "배정할 회원 ID 목록"),
                                arrayFieldWithPath("teamIds", "담당 팀 ID 목록")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 배정된 담당 팀 목록을 수정한다.")
    void 배정된_담당_팀_목록을_수정한다() throws Exception {
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(12L),
                List.of(10L));

        doNothing().when(contestMemberCommandService).updateAssignedTeams(any(), any(), any());

        mockMvc.perform(patch("/contests/{contestId}/staff/{contestMemberId}", 1, 2)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-contest-member",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("contestMemberId").description("배정 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                arrayFieldWithPath("addTeamIds", "추가할 팀 ID 목록"),
                                arrayFieldWithPath("deleteTeamIds", "삭제할 팀 ID 목록")
                        )
                ));
    }
}
