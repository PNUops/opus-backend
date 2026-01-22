package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @Test
    @DisplayName("[성공] 투표 기간을 조회하면 시작일과 종료일을 반환한다.")
    void 투표_기간을_조회하면_시작일과_종료일을_반환한다() throws Exception {
        VotePeriodResponse response = new VotePeriodResponse(
                LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTime.of(2026, 1, 2, 0, 0, 0)
        );

        given(contestQueryService.getVotePeriod(any())).willReturn(response);

        mockMvc.perform(get("/contests/{contestId}/vote", 1L))
                .andExpect(status().isOk())
                .andDo(document("get-vote-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        responseFields(
                                dateTimeFieldWithPath("voteStartAt", "투표 시작일"),
                                dateTimeFieldWithPath("voteEndAt", "투표 종료일")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 관리자는 투표 기간을 수정할 수 있다.")
    void 관리자는_투표_기간을_수정할_수_있다() throws Exception {
        VoteUpdateRequest request = new VoteUpdateRequest(
                LocalDateTime.of(2026, 2, 1, 0, 0, 0),
                LocalDateTime.of(2026, 2, 10, 0, 0, 0)
        );

        doNothing().when(contestCommandService).updateVotePeriod(any(), any());

        mockMvc.perform(put("/contests/{contestId}/vote", 1L)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("update-vote-period",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("관리자 액세스 토큰")
                        ),
                        requestFields(
                                dateTimeFieldWithPath("voteStartAt", "투표 시작일"),
                                dateTimeFieldWithPath("voteEndAt", "투표 종료일")
                        )
                ));
    }
}
