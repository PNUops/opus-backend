package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static java.time.LocalDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionStatusResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class AdminContestApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @Test
    @DisplayName("[성공] 제출 항목 기준으로 전체 팀의 제출 현황을 조회한다.")
    void 제출물_제출_현황을_조회한다() throws Exception {
        final List<ContestSubmissionStatusResponse> responses = List.of(
                new ContestSubmissionStatusResponse(12L, 3L, "오퍼스", "AI/데이터", "최종 발표 자료",
                        SubmissionStatus.SUBMITTED, now().minusDays(2), now().minusDays(1)),
                new ContestSubmissionStatusResponse(null, 4L, "팀B", "AI/데이터", "최종 발표 자료",
                        SubmissionStatus.NOT_SUBMITTED, null, null));

        when(contestSubmissionQueryService.getSubmissionStatuses(any(), any(), any(), any(), any()))
                .thenReturn(responses);

        mockMvc.perform(get("/admin/contests/{contestId}/submissions", 1)
                        .param("submissionItemId", "3")
                        .param("status", "SUBMITTED")
                        .param("trackId", "2")
                        .param("keyword", "오퍼스")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-admin-contest-submissions",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        queryParameters(
                                parameterWithName("submissionItemId").description("제출 항목 ID (미지정 시 전체 항목)")
                                        .optional(),
                                parameterWithName("status").description(
                                        "제출 상태 필터 (SUBMITTED / LATE / NOT_SUBMITTED / NOT_SUBMITTED_AFTER_DEADLINE)")
                                        .optional(),
                                parameterWithName("trackId").description("분과 ID").optional(),
                                parameterWithName("keyword").description("팀 이름 검색어").optional()
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                numberFieldWithPath("[].submissionId", "제출 ID (미제출이면 null)").optional(),
                                numberFieldWithPath("[].teamId", "팀 ID"),
                                stringFieldWithPath("[].teamName", "팀 이름"),
                                stringFieldWithPath("[].trackName", "분과명"),
                                stringFieldWithPath("[].submissionItemName", "제출 항목명"),
                                stringFieldWithPath("[].status", "제출 상태 (계산값)"),
                                dateTimeFieldWithPath("[].firstSubmittedAt", "최초 제출일시 (미제출이면 null)")
                                        .optional(),
                                dateTimeFieldWithPath("[].lastModifiedAt", "마지막 수정일시 (미제출이면 null)")
                                        .optional())));
    }

    @Test
    @DisplayName("[성공] 제출 항목·분과 기준으로 제출 현황 통계를 조회한다.")
    void 제출_현황_통계를_조회한다() throws Exception {
        final ContestSubmissionSummaryResponse summary = new ContestSubmissionSummaryResponse(24, 18, 4, 2);

        when(contestSubmissionQueryService.getSubmissionSummary(any(), any(), any()))
                .thenReturn(summary);

        mockMvc.perform(get("/admin/contests/{contestId}/submissions/summary", 1)
                        .param("submissionItemId", "3")
                        .param("trackId", "2")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-admin-contest-submissions-summary",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        queryParameters(
                                parameterWithName("submissionItemId").description("제출 항목 ID (미지정 시 전체 항목)")
                                        .optional(),
                                parameterWithName("trackId").description("분과 ID").optional()
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                numberFieldWithPath("totalTeams", "전체 팀 수"),
                                numberFieldWithPath("submittedCount", "제출 완료 팀 수 (마감 전 제출)"),
                                numberFieldWithPath("notSubmittedCount", "미제출 팀 수 (마감 후 미제출 포함)"),
                                numberFieldWithPath("lateCount", "지각 제출 팀 수"))));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 제출 현황 통계를 조회하면 실패한다.")
    void 존재하지_않는_대회_제출_현황_통계_조회_실패() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestSubmissionQueryService)
                .getSubmissionSummary(any(), any(), any());

        mockMvc.perform(get("/admin/contests/{contestId}/submissions/summary", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-admin-contest-submissions-summary-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 제출 현황을 조회하면 실패한다.")
    void 존재하지_않는_대회_제출_현황_조회_실패() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestSubmissionQueryService)
                .getSubmissionStatuses(any(), any(), any(), any(), any());

        mockMvc.perform(get("/admin/contests/{contestId}/submissions", 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-admin-contest-submissions-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID")
                        )));
    }
}
