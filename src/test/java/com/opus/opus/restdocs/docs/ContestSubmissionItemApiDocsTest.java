package com.opus.opus.restdocs.docs;

import static com.opus.opus.contest.ContestSubmissionItemFixture.createRequest;
import static com.opus.opus.contest.ContestSubmissionItemFixture.createRequestWithPeriod;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_PERIOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
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

        request = createRequest(1L);
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
    @DisplayName("[실패] 시작일시가 마감일시보다 이후면 에러를 반환한다.")
    void 시작일시가_마감일시보다_이후면_에러를_반환한다() throws Exception {
        final ContestSubmissionItemRequest invalidRequest = createRequestWithPeriod(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0)
        );

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
