package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.request.ContestVotesLimitRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.restdocs.RestDocsTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @Test
    @DisplayName("[성공] 유효한 요청이면 최대 투표 개수가 정상적으로 설정된다.")
    void 유효한_요청이면_최대_투표_개수가_정상적으로_설정된다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        doNothing().when(contestCommandService).updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-max-votes-limit",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 Bearer {accessToken}")
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수 설정 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_최대_투표_개수_설정_시_에러를_반환한다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestCommandService)
                .updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("update-max-votes-limit-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 Bearer {accessToken}")
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 투표 진행 중 최대 투표 개수 변경 시 400 에러를 반환한다.")
    void 투표_진행_중_최대_투표_개수_변경_시_에러를_반환한다() throws Exception {
        final ContestVotesLimitRequest request = new ContestVotesLimitRequest(2);

        willThrow(new ContestException(CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD))
                .given(contestCommandService)
                .updateMaxVotesLimit(any(), any());

        mockMvc.perform(patch("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("update-max-votes-limit-fail-voting-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 Bearer {accessToken}")
                        ),
                        requestFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수를 정상적으로 조회할 수 있다.")
    void 최대_투표_개수를_정상적으로_조회할_수_있다() throws Exception {
        final ContestVotesLimitResponse response = new ContestVotesLimitResponse(2);

        when(contestQueryService.getMaxVotesLimit(any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/votes", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-max-votes-limit",
                        pathParameters(
                                parameterWithName("contestId").description("대회의 고유 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 Bearer {accessToken}")
                        ),
                        responseFields(
                                numberFieldWithPath("maxVotesLimit", "최대 투표 개수")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_최대_투표_개수_조회_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestQueryService)
                .getMaxVotesLimit(any());

        mockMvc.perform(get("/contests/{contestId}/votes", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-max-votes-limit-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 Bearer {accessToken}")
                        )
                ));
    }
}
