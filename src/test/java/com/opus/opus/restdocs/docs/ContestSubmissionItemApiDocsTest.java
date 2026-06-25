package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.PDF;
import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.ZIP;
import static com.opus.opus.modules.contest.domain.SubmissionVisibility.PUBLIC;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_PERIOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestSubmissionItemApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    private Member admin;
    private ContestSubmissionItemRequest request;

    @BeforeEach
    void setUp() {
        this.admin = MemberFixture.createMember();
        setField(admin, "id", 1L);

        request = new ContestSubmissionItemRequest(
                "발표자료", 1L, "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59), true, PUBLIC);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출 항목 생성은 성공한다.")
    void 유효한_요청이면_제출_항목_생성은_성공한다() throws Exception {
        doNothing().when(contestSubmissionItemCommandService).createSubmissionItem(any(), any());

        mockMvc.perform(post("/contests/{contestId}/submission-items", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-submission-item",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("name", "제출물 종류 이름"),
                                numberFieldWithPath("contestTrackId", "대상 분과 ID (생략 시 전체 분과)").optional(),
                                stringFieldWithPath("description", "설명").optional(),
                                arrayFieldWithPath("allowedFileFormats", "허용 파일 형식 목록 (SubmissionFileFormat)"),
                                numberFieldWithPath("maxFileSizeMb", "파일 크기 제한 (MB)"),
                                numberFieldWithPath("maxFileCount", "파일 수 제한"),
                                dateTimeFieldWithPath("startAt", "시작일시"),
                                dateTimeFieldWithPath("endAt", "마감일시"),
                                booleanFieldWithPath("allowLateSubmission", "지각 제출 허용 여부"),
                                stringFieldWithPath("visibility", "공개 범위 (SubmissionVisibility)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출 항목 수정은 성공한다.")
    void 유효한_요청이면_제출_항목_수정은_성공한다() throws Exception {
        doNothing().when(contestSubmissionItemCommandService).updateSubmissionItem(any(), any(), any());

        mockMvc.perform(patch("/contests/{contestId}/submission-items/{submissionItemId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-submission-item",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("name", "제출물 종류 이름"),
                                numberFieldWithPath("contestTrackId", "대상 분과 ID (생략 시 전체 분과)").optional(),
                                stringFieldWithPath("description", "설명").optional(),
                                arrayFieldWithPath("allowedFileFormats", "허용 파일 형식 목록 (SubmissionFileFormat)"),
                                numberFieldWithPath("maxFileSizeMb", "파일 크기 제한 (MB)"),
                                numberFieldWithPath("maxFileCount", "파일 수 제한"),
                                dateTimeFieldWithPath("startAt", "시작일시"),
                                dateTimeFieldWithPath("endAt", "마감일시"),
                                booleanFieldWithPath("allowLateSubmission", "지각 제출 허용 여부"),
                                stringFieldWithPath("visibility", "공개 범위 (SubmissionVisibility)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출 항목 삭제는 성공한다.")
    void 유효한_요청이면_제출_항목_삭제는_성공한다() throws Exception {
        doNothing().when(contestSubmissionItemCommandService).deleteSubmissionItem(any(), any());

        mockMvc.perform(delete("/contests/{contestId}/submission-items/{submissionItemId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-submission-item",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출 항목 설정값 조회는 성공한다.")
    void 유효한_요청이면_제출_항목_설정값_조회는_성공한다() throws Exception {
        final ContestSubmissionItemResponse response = new ContestSubmissionItemResponse(
                "발표자료", 1L, "PDF 형식의 발표자료를 제출하세요.", List.of("PDF", "ZIP"),
                50, 3, LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59), true, "PUBLIC");

        when(contestSubmissionItemQueryService.getSubmissionItem(any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/submission-items/{submissionItemId}", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-item",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        responseFields(
                                stringFieldWithPath("name", "제출물 종류 이름"),
                                numberFieldWithPath("contestTrackId", "대상 분과 ID (전체 분과면 null)").optional(),
                                stringFieldWithPath("description", "설명").optional(),
                                arrayFieldWithPath("allowedFileFormats", "허용 파일 형식 목록 (SubmissionFileFormat)"),
                                numberFieldWithPath("maxFileSizeMb", "파일 크기 제한 (MB)"),
                                numberFieldWithPath("maxFileCount", "파일 수 제한"),
                                dateTimeFieldWithPath("startAt", "시작일시"),
                                dateTimeFieldWithPath("endAt", "마감일시"),
                                booleanFieldWithPath("allowLateSubmission", "지각 제출 허용 여부"),
                                stringFieldWithPath("visibility", "공개 범위 (SubmissionVisibility)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 시작일시가 마감일시보다 이후면 에러를 반환한다.")
    void 시작일시가_마감일시보다_이후면_에러를_반환한다() throws Exception {
        final ContestSubmissionItemRequest invalidRequest = new ContestSubmissionItemRequest(
                "발표자료", null, "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 7, 1, 0, 0), true, PUBLIC);

        willThrow(new ContestSubmissionItemException(INVALID_SUBMISSION_PERIOD))
                .given(contestSubmissionItemCommandService).createSubmissionItem(any(), any());

        mockMvc.perform(post("/contests/{contestId}/submission-items", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-submission-item-fail",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")
                        ),
                        requestFields(
                                stringFieldWithPath("name", "제출물 종류 이름"),
                                numberFieldWithPath("contestTrackId", "대상 분과 ID (생략 시 전체 분과)").optional(),
                                stringFieldWithPath("description", "설명").optional(),
                                arrayFieldWithPath("allowedFileFormats", "허용 파일 형식 목록 (SubmissionFileFormat)"),
                                numberFieldWithPath("maxFileSizeMb", "파일 크기 제한 (MB)"),
                                numberFieldWithPath("maxFileCount", "파일 수 제한"),
                                dateTimeFieldWithPath("startAt", "시작일시 (마감일시보다 이후)"),
                                dateTimeFieldWithPath("endAt", "마감일시"),
                                booleanFieldWithPath("allowLateSubmission", "지각 제출 허용 여부"),
                                stringFieldWithPath("visibility", "공개 범위 (SubmissionVisibility)")
                        )
                ));
    }
}
