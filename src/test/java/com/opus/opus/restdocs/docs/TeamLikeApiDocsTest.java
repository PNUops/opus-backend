package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_LIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_UNLIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.NOT_LIKED_YET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.dto.request.TeamLikeToggleRequest;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamLikeException;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TeamLikeApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 팀에 좋아요를 등록한다.")
    void 팀에_좋아요를_등록한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(true);
        final TeamLikeToggleResponse response = new TeamLikeToggleResponse(1L, true, "좋아요가 등록되었습니다.");

        given(teamLikeCommandService.toggleLike(any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("toggle-team-like",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부 (true: 등록, false: 취소)")
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "팀 ID"),
                                booleanFieldWithPath("isLiked", "좋아요 상태"),
                                stringFieldWithPath("message", "응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀 좋아요를 취소한다.")
    void 팀_좋아요를_취소한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(false);
        final TeamLikeToggleResponse response = new TeamLikeToggleResponse(1L, false, "좋아요가 취소되었습니다.");

        given(teamLikeCommandService.toggleLike(any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("cancel-team-like",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요를 취소할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부 (true: 등록, false: 취소)")
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "팀 ID"),
                                booleanFieldWithPath("isLiked", "좋아요 상태"),
                                stringFieldWithPath("message", "응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에 좋아요 시 404 에러를 반환한다.")
    void 존재하지_않는_팀에_좋아요_시_에러를_반환한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(true);

        willThrow(new TeamException(NOT_FOUND_TEAM))
                .given(teamLikeCommandService)
                .toggleLike(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 999)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("toggle-team-like-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 좋아요한 팀에 다시 좋아요 시 400 에러를 반환한다.")
    void 이미_좋아요한_팀에_다시_좋아요_시_에러를_반환한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(true);

        willThrow(new TeamLikeException(ALREADY_LIKED))
                .given(teamLikeCommandService)
                .toggleLike(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-like-fail-already-liked",
                        pathParameters(
                                parameterWithName("teamId").description("이미 좋아요한 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 좋아요 취소한 팀에 다시 취소 시 400 에러를 반환한다.")
    void 이미_좋아요_취소한_팀에_다시_취소_시_에러를_반환한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(false);

        willThrow(new TeamLikeException(ALREADY_UNLIKED))
                .given(teamLikeCommandService)
                .toggleLike(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-like-fail-already-unliked",
                        pathParameters(
                                parameterWithName("teamId").description("이미 좋아요 취소한 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 좋아요한 적 없는 팀에 취소 요청 시 400 에러를 반환한다.")
    void 좋아요한_적_없는_팀에_취소_요청_시_에러를_반환한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(false);

        willThrow(new TeamLikeException(NOT_LIKED_YET))
                .given(teamLikeCommandService)
                .toggleLike(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-like-fail-not-liked-yet",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요한 적 없는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 기간에 좋아요 시 400 에러를 반환한다.")
    void 투표_기간에_좋아요_시_에러를_반환한다() throws Exception {
        final TeamLikeToggleRequest request = new TeamLikeToggleRequest(true);

        willThrow(new ContestException(NOT_ALLOWED_DURING_VOTING_PERIOD))
                .given(teamLikeCommandService)
                .toggleLike(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-like-fail-voting-period",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isLiked", "좋아요 여부")
                        )
                ));
    }
}
