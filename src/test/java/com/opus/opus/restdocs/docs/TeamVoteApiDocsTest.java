package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_VOTE_PERIOD_NOW;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_UNVOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_VOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.NOT_VOTED_YET;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.VOTE_LIMIT_EXCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
import com.opus.opus.modules.team.application.dto.request.TeamVoteToggleRequest;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamVoteException;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class TeamVoteApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 팀에 투표를 등록한다.")
    void 팀에_투표를_등록한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(true);
        final TeamVoteToggleResponse response = new TeamVoteToggleResponse(1L, true, "투표가 등록되었습니다.", 1L, 2L);

        given(teamCommandService.toggleVote(any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("toggle-team-vote",
                        pathParameters(
                                parameterWithName("teamId").description("투표할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부 (true: 등록, false: 취소)")
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "팀 ID"),
                                booleanFieldWithPath("isVoted", "투표 상태"),
                                stringFieldWithPath("message", "응답 메시지"),
                                numberFieldWithPath("remainingVotesCount", "남은 투표 가능 횟수"),
                                numberFieldWithPath("maxVotesLimit", "대회 최대 투표 허용 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀 투표를 취소한다.")
    void 팀_투표를_취소한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(false);
        final TeamVoteToggleResponse response = new TeamVoteToggleResponse(1L, false, "투표가 취소되었습니다.", 2L, 2L);

        given(teamCommandService.toggleVote(any(), any(), any())).willReturn(response);

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("cancel-team-vote",
                        pathParameters(
                                parameterWithName("teamId").description("투표를 취소할 팀의 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부 (true: 등록, false: 취소)")
                        ),
                        responseFields(
                                numberFieldWithPath("teamId", "팀 ID"),
                                booleanFieldWithPath("isVoted", "투표 상태"),
                                stringFieldWithPath("message", "응답 메시지"),
                                numberFieldWithPath("remainingVotesCount", "남은 투표 가능 횟수"),
                                numberFieldWithPath("maxVotesLimit", "대회 최대 투표 허용 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에 투표 시 404 에러를 반환한다.")
    void 존재하지_않는_팀에_투표_시_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(true);

        willThrow(new TeamException(NOT_FOUND_TEAM))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 999)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("toggle-team-vote-fail-not-found",
                        pathParameters(
                                parameterWithName("teamId").description("존재하지 않는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 투표한 팀에 다시 투표 시 400 에러를 반환한다.")
    void 이미_투표한_팀에_다시_투표_시_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(true);

        willThrow(new TeamVoteException(ALREADY_VOTED))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-vote-fail-already-voted",
                        pathParameters(
                                parameterWithName("teamId").description("이미 투표한 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 투표 취소한 팀에 다시 취소 시 400 에러를 반환한다.")
    void 이미_투표_취소한_팀에_다시_취소_시_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(false);

        willThrow(new TeamVoteException(ALREADY_UNVOTED))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-vote-fail-already-unvoted",
                        pathParameters(
                                parameterWithName("teamId").description("이미 투표 취소한 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표한 적 없는 팀에 취소 요청 시 400 에러를 반환한다.")
    void 투표한_적_없는_팀에_취소_요청_시_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(false);

        willThrow(new TeamVoteException(NOT_VOTED_YET))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-vote-fail-not-voted-yet",
                        pathParameters(
                                parameterWithName("teamId").description("투표한 적 없는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 최대 투표 수 초과 시 400 에러를 반환한다.")
    void 최대_투표_수_초과_시_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(true);

        willThrow(new TeamVoteException(VOTE_LIMIT_EXCEEDED, "대회당 최대 2개 팀만 투표할 수 있습니다."))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 3)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-vote-fail-limit-exceeded",
                        pathParameters(
                                parameterWithName("teamId").description("투표하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 기간이 아닐 때 400 에러를 반환한다.")
    void 투표_기간이_아닐_때_에러를_반환한다() throws Exception {
        final TeamVoteToggleRequest request = new TeamVoteToggleRequest(true);

        willThrow(new ContestException(NOT_VOTE_PERIOD_NOW))
                .given(teamCommandService)
                .toggleVote(any(), any(), any());

        mockMvc.perform(put("/teams/{teamId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("toggle-team-vote-fail-not-vote-period",
                        pathParameters(
                                parameterWithName("teamId").description("투표하려는 팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestFields(
                                booleanFieldWithPath("isVoted", "투표 여부")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 사용자의 투표 개수를 조회한다.")
    void 사용자의_투표_개수를_조회한다() throws Exception {
        final MemberVoteCountResponse response = new MemberVoteCountResponse(1L, 2L);

        given(contestQueryService.getMemberVoteCount(any(), any())).willReturn(response);

        mockMvc.perform(get("/contests/{contestId}/votes/me", 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-member-vote-count",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                numberFieldWithPath("remainingVotesCount", "남은 투표 가능 횟수"),
                                numberFieldWithPath("maxVotesLimit", "대회 최대 투표 허용 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회 ID로 투표 개수 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회_ID로_투표_개수_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getMemberVoteCount(any(), any());

        mockMvc.perform(get("/contests/{contestId}/votes/me", 999)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-member-vote-count-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }
}
