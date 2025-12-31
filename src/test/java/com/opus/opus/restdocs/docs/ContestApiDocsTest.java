package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;

public class ContestApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";
    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 비회원이 대회의 팀 목록을 조회할 수 있다.")
    void 비회원이_대회의_팀_목록을_조회할_수_있다() throws Exception {
        final List<TeamSummaryResponse.AwardInfo> awards1 = List.of(
                new TeamSummaryResponse.AwardInfo("대상", "#FF0000"),
                new TeamSummaryResponse.AwardInfo("우수상", "#00A3FF")
        );
        final List<TeamSummaryResponse.AwardInfo> awards2 = List.of();

        final List<TeamSummaryResponse> responses = List.of(
                new TeamSummaryResponse(1L, "team1", "team1 Project", false, null, awards1),
                new TeamSummaryResponse(2L, "team2", "team2 Project", false, null, awards2)
        );

        when(contestQueryService.getContestTeamSummaries(anyLong(), any())).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/teams", 1L))
                .andExpect(status().isOk())
                .andDo(document("get-contest-team-summaries",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "팀 목록"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                booleanFieldWithPath("[].isLiked", "좋아요 여부 (미투표 기간, 비회원은 항상 false)"),
                                fieldWithPath("[].isVoted").optional().type(JsonFieldType.BOOLEAN).description("투표 여부 (투표 기간인 경우, 미투표 기간에는 null)"),
                                arrayFieldWithPath("[].awards", "수상 목록"),
                                stringFieldWithPath("[].awards[].awardName", "수상명"),
                                stringFieldWithPath("[].awards[].awardColor", "수상 색상")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 회원이 대회의 팀 목록을 조회할 수 있다. (미투표 기간)")
    void 회원이_대회의_팀_목록을_조회할_수_있다_미투표_기간() throws Exception {
        final List<TeamSummaryResponse.AwardInfo> awards1 = List.of(
                new TeamSummaryResponse.AwardInfo("대상", "#FF0000")
        );
        final List<TeamSummaryResponse.AwardInfo> awards2 = List.of();

        final List<TeamSummaryResponse> responses = List.of(
                new TeamSummaryResponse(1L, "team1", "team1 Project", true, null, awards1),
                new TeamSummaryResponse(2L, "team2", "team2 Project", false, null, awards2)
        );

        when(contestQueryService.getContestTeamSummaries(anyLong(), any(Member.class))).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/teams", 1L)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-contest-team-summaries-with-auth",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (선택)")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "팀 목록"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                booleanFieldWithPath("[].isLiked", "좋아요 여부 (미투표 기간, 회원은 로그인한 사용자의 좋아요 여부에 따라)"),
                                fieldWithPath("[].isVoted").optional().type(JsonFieldType.BOOLEAN).description("투표 여부 (투표 기간인 경우, 미투표 기간에는 null)"),
                                arrayFieldWithPath("[].awards", "수상 목록"),
                                stringFieldWithPath("[].awards[].awardName", "수상명"),
                                stringFieldWithPath("[].awards[].awardColor", "수상 색상")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 회원이 대회의 팀 목록을 조회할 수 있다. (투표 기간)")
    void 회원이_대회의_팀_목록을_조회할_수_있다_투표_기간() throws Exception {
        final List<TeamSummaryResponse.AwardInfo> awards1 = List.of(
                new TeamSummaryResponse.AwardInfo("대상", "#FF0000"),
                new TeamSummaryResponse.AwardInfo("우수상", "#00A3FF")
        );
        final List<TeamSummaryResponse.AwardInfo> awards2 = List.of();

        final List<TeamSummaryResponse> responses = List.of(
                new TeamSummaryResponse(1L, "team1", "team1 Project", null, true, awards1),
                new TeamSummaryResponse(2L, "team2", "team2 Project", null, false, awards2)
        );

        when(contestQueryService.getContestTeamSummaries(anyLong(), any(Member.class))).thenReturn(responses);

        mockMvc.perform(get("/contests/{contestId}/teams", 1L)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-contest-team-summaries-with-auth-voting",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (선택)")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "팀 목록"),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀명"),
                                stringFieldWithPath("[].projectName", "프로젝트명"),
                                fieldWithPath("[].isLiked").optional().type(JsonFieldType.BOOLEAN).description("좋아요 여부 (미투표 기간인 경우, 투표 기간에는 null)"),
                                booleanFieldWithPath("[].isVoted", "투표 여부 (투표 기간인 경우, 회원은 로그인한 사용자의 투표 여부에 따라)"),
                                arrayFieldWithPath("[].awards", "수상 목록"),
                                stringFieldWithPath("[].awards[].awardName", "수상명"),
                                stringFieldWithPath("[].awards[].awardColor", "수상 색상")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회 ID로 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회_ID로_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getContestTeamSummaries(anyLong(), any());

        mockMvc.perform(get("/contests/{contestId}/teams", 999L))
                .andExpect(status().isNotFound())
                .andDo(document("get-contest-team-summaries-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )
                ));
    }
}
