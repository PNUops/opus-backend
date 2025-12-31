package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TeamCommentApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";
    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any(MethodParameter.class)))
                .thenReturn(true);
        when(memberArgumentResolver.resolveArgument(
                any(MethodParameter.class),
                any(ModelAndViewContainer.class),
                any(NativeWebRequest.class),
                any(WebDataBinderFactory.class)
        )).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 팀 댓글이 정상적으로 등록된다.")
    void 유효한_요청이면_팀_댓글이_정상적으로_등록된다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("정말 멋진 프로젝트네요!");

        doNothing().when(teamCommentCommandService).createComment(any(), any(), any());

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
                                stringFieldWithPath("description", "댓글 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에 댓글 등록 시 404 에러를 반환한다.")
    void 존재하지_않는_팀에_댓글_등록_시_에러를_반환한다() throws Exception {
        final TeamCommentCreateRequest request = new TeamCommentCreateRequest("정말 멋진 프로젝트네요!");

        willThrow(new TeamException(NOT_FOUND_TEAM))
                .given(teamCommentCommandService)
                .createComment(any(), any(), any());

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
                                stringFieldWithPath("description", "댓글 내용")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 댓글 목록을 정상적으로 조회할 수 있다.")
    void 팀의_댓글_목록을_정상적으로_조회할_수_있다() throws Exception {
        final List<TeamCommentResponse> responses = List.of(
                new TeamCommentResponse(1L, "정말 멋진 프로젝트네요!", 1L, "이옵스", 1L),
                new TeamCommentResponse(2L, "고생하셨습니다!", 2L, "김옵스", 1L)
        );

        when(teamCommentQueryService.getComments(any())).thenReturn(responses);

        mockMvc.perform(get("/teams/{teamId}/comments", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-team-comments",
                        pathParameters(
                                parameterWithName("teamId").description("댓글을 조회할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "댓글 목록"),
                                numberFieldWithPath("[].commentId", "댓글 ID"),
                                stringFieldWithPath("[].description", "댓글 내용"),
                                numberFieldWithPath("[].memberId", "작성자 ID"),
                                stringFieldWithPath("[].memberName", "작성자 이름"),
                                numberFieldWithPath("[].teamId", "팀 ID")
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
