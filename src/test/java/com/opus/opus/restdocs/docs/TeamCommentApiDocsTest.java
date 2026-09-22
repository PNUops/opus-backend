package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.PUBLIC;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.TEAM;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.COMMENT_LENGTH_EXCEEDED;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.dto.request.TeamCommentCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCommentUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCommentResponse;
import com.opus.opus.modules.team.exception.TeamCommentException;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TeamCommentApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";
    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 팀 댓글이 정상적으로 등록된다.")
    void 유효한_요청이면_팀_댓글이_정상적으로_등록된다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("정말 멋진 프로젝트네요!", PUBLIC);

        doNothing().when(teamCommentCommandService).createComment(any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-team-comment",
                        pathParameters(
                                parameterWithName("teamId").description("댓글을 등록할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "댓글 내용"),
                                stringFieldWithPath("visibility", "공개 범위 (PUBLIC: 공개 댓글, TEAM: 교수/외부멘토의 팀 피드백 — 팀 구성원/관리자/작성자만 조회, 미지정 시 PUBLIC)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에 댓글 등록 시 404 에러를 반환한다.")
    void 존재하지_않는_팀에_댓글_등록_시_에러를_반환한다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("정말 멋진 프로젝트네요!", PUBLIC);

        willThrow(new TeamException(NOT_FOUND_TEAM))
                .given(teamCommentCommandService)
                .createComment(any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/comments", 999)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("create-team-comment-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "댓글 내용"),
                                stringFieldWithPath("visibility", "공개 범위 (PUBLIC: 공개 댓글, TEAM: 교수/외부멘토의 팀 피드백 — 팀 구성원/관리자/작성자만 조회, 미지정 시 PUBLIC)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 교수 또는 외부멘토가 아닌 회원이 팀 피드백 등록 시 403 에러를 반환한다.")
    void 교수_또는_외부멘토가_아닌_회원이_팀_피드백_등록_시_에러를_반환한다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("발표 흐름이 명확해서 좋았습니다.", TEAM);

        willThrow(new TeamCommentException(NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT))
                .given(teamCommentCommandService)
                .createComment(any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andDo(document("create-team-comment-fail-not-staff",
                        pathParameters(
                                parameterWithName("teamId").description("댓글을 등록할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "댓글 내용"),
                                stringFieldWithPath("visibility", "공개 범위 (TEAM: 팀 피드백)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 공개 범위의 최대 글자 수를 초과한 댓글 등록 시 400 에러를 반환한다.")
    void 공개_범위의_최대_글자_수를_초과한_댓글_등록_시_에러를_반환한다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("a".repeat(256), PUBLIC);

        willThrow(new TeamCommentException(COMMENT_LENGTH_EXCEEDED, "댓글은 최대 255자까지 작성할 수 있습니다."))
                .given(teamCommentCommandService)
                .createComment(any(), any(), any(), any());

        mockMvc.perform(post("/teams/{teamId}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-team-comment-fail-length-exceeded",
                        pathParameters(
                                parameterWithName("teamId").description("댓글을 등록할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "댓글 내용 (PUBLIC: 최대 255자, TEAM: 최대 3000자)"),
                                stringFieldWithPath("visibility", "공개 범위 (PUBLIC: 공개 댓글, TEAM: 팀 피드백)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 댓글 목록을 정상적으로 조회할 수 있다.")
    void 팀의_댓글_목록을_정상적으로_조회할_수_있다() throws Exception {
        final LocalDateTime now = LocalDateTime.of(2026, 9, 30, 10, 0, 0);
        final List<TeamCommentResponse> responses = List.of(
                new TeamCommentResponse(2L, "발표 흐름이 명확해서 좋았습니다. 시장 분석 근거를 보완해 보세요.", TEAM, 2L, "김교수",
                        ROLE_교수.name(), 1L, now, now),
                new TeamCommentResponse(1L, "정말 멋진 프로젝트네요!", PUBLIC, 1L, "이옵스", null, 1L, now, now)
        );

        when(teamCommentQueryService.getComments(any(), any(), any())).thenReturn(responses);

        mockMvc.perform(get("/teams/{teamId}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-team-comments",
                        pathParameters(
                                parameterWithName("teamId").description("댓글을 조회할 팀의 ID")
                        ),
                        queryParameters(
                                parameterWithName("visibility").optional()
                                        .description("공개 범위 필터 (PUBLIC / TEAM, 미지정 시 전체)")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "댓글 목록 (TEAM 팀 피드백은 팀 구성원/관리자/작성자에게만 포함)"),
                                numberFieldWithPath("[].commentId", "댓글 ID"),
                                stringFieldWithPath("[].description", "댓글 내용"),
                                stringFieldWithPath("[].visibility", "공개 범위 (PUBLIC: 공개 댓글, TEAM: 팀 피드백)"),
                                numberFieldWithPath("[].memberId", "작성자 ID"),
                                stringFieldWithPath("[].memberName", "작성자 이름"),
                                stringFieldWithPath("[].memberRoleType", "작성자 역할 (교수/외부멘토만, 그 외 null)").optional(),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                dateTimeFieldWithPath("[].createdAt", "작성 일시"),
                                dateTimeFieldWithPath("[].updatedAt", "수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 팀 댓글이 정상적으로 수정된다.")
    void 유효한_요청이면_팀_댓글이_정상적으로_수정된다() throws Exception {
        final TeamCommentUpdateRequest request = new TeamCommentUpdateRequest("수정된 댓글 내용입니다.");

        doNothing().when(teamCommentCommandService).updateComment(any(), any(), any(), any());

        mockMvc.perform(patch("/teams/{teamId}/comments/{commentId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("update-team-comment",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("commentId").description("수정할 댓글 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "수정할 댓글 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 댓글 수정 시 403 에러를 반환한다.")
    void 본인이_작성하지_않은_댓글_수정_시_에러를_반환한다() throws Exception {
        final TeamCommentUpdateRequest request = new TeamCommentUpdateRequest("수정된 댓글 내용입니다.");

        willThrow(new TeamCommentException(NOT_OWNER_COMMENT))
                .given(teamCommentCommandService)
                .updateComment(any(), any(), any(), any());

        mockMvc.perform(patch("/teams/{teamId}/comments/{commentId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andDo(document("update-team-comment-fail-not-owner",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("commentId").description("다른 사람이 작성한 댓글 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "수정할 댓글 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 공개 범위의 최대 글자 수를 초과한 댓글 수정 시 400 에러를 반환한다.")
    void 공개_범위의_최대_글자_수를_초과한_댓글_수정_시_에러를_반환한다() throws Exception {
        final TeamCommentUpdateRequest request = new TeamCommentUpdateRequest("a".repeat(256));

        willThrow(new TeamCommentException(COMMENT_LENGTH_EXCEEDED, "댓글은 최대 255자까지 작성할 수 있습니다."))
                .given(teamCommentCommandService)
                .updateComment(any(), any(), any(), any());

        mockMvc.perform(patch("/teams/{teamId}/comments/{commentId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-team-comment-fail-length-exceeded",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("commentId").description("수정할 댓글 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                stringFieldWithPath("description", "수정할 댓글 내용 (댓글의 공개 범위 기준 PUBLIC: 최대 255자, TEAM: 최대 3000자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀 댓글이 정상적으로 삭제된다.")
    void 팀_댓글이_정상적으로_삭제된다() throws Exception {
        doNothing().when(teamCommentCommandService).deleteComment(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/comments/{commentId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-team-comment",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("commentId").description("삭제할 댓글 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 댓글 삭제 시 403 에러를 반환한다.")
    void 본인이_작성하지_않은_댓글_삭제_시_에러를_반환한다() throws Exception {
        willThrow(new TeamCommentException(NOT_OWNER_COMMENT))
                .given(teamCommentCommandService)
                .deleteComment(any(), any(), any());

        mockMvc.perform(delete("/teams/{teamId}/comments/{commentId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isForbidden())
                .andDo(document("delete-team-comment-fail-not-owner",
                        pathParameters(
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("commentId").description("다른 사람이 작성한 댓글 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }
}
