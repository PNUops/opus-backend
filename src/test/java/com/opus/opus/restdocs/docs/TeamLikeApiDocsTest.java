package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.DUPLICATE_LIKE_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamLikeException;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

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
        willDoNothing().given(teamCommandService).addLike(any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("add-team-like",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀 좋아요를 취소한다.")
    void 팀_좋아요를_취소한다() throws Exception {
        willDoNothing().given(teamCommandService).removeLike(any(), any());

        mockMvc.perform(delete("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("remove-team-like",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요를 취소할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에 좋아요 시 404 에러를 반환한다.")
    void 존재하지_않는_팀에_좋아요_시_에러를_반환한다() throws Exception {
        willThrow(new TeamException(NOT_FOUND_TEAM))
                .given(teamCommandService)
                .addLike(any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 999)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("add-team-like-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 기간에 좋아요 등록 시 400 에러를 반환한다.")
    void 투표_기간에_좋아요_등록_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_ALLOWED_DURING_VOTING_PERIOD))
                .given(teamCommandService)
                .addLike(any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isBadRequest())
                .andDo(document("add-team-like-fail-voting-period",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 기간에 좋아요 취소 시 400 에러를 반환한다.")
    void 투표_기간에_좋아요_취소_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_ALLOWED_DURING_VOTING_PERIOD))
                .given(teamCommandService)
                .removeLike(any(), any());

        mockMvc.perform(delete("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isBadRequest())
                .andDo(document("remove-team-like-fail-voting-period",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요를 취소하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 동시 중복 요청 시 409 에러를 반환한다.")
    void 동시_중복_요청_시_에러를_반환한다() throws Exception {
        willThrow(new TeamLikeException(DUPLICATE_LIKE_REQUEST))
                .given(teamCommandService)
                .addLike(any(), any());

        mockMvc.perform(put("/teams/{teamId}/likes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isConflict())
                .andDo(document("add-team-like-fail-duplicate",
                        pathParameters(
                                parameterWithName("teamId").description("좋아요하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }
}
