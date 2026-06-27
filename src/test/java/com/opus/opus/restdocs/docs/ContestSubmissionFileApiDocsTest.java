package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.request.SubmissionDownloadRequest;
import com.opus.opus.modules.contest.application.dto.request.DownloadTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionDownload;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class ContestSubmissionFileApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @BeforeEach
    void setUp() {
        final Member admin = MemberFixture.createMember();
        setField(admin, "id", 1L);
    }

    @Test
    @DisplayName("[성공] 제출 파일 다운로드 대상 목록을 조회한다.")
    void 제출_파일_다운로드_대상_목록을_조회한다() throws Exception {
        final DownloadTargetsResponse response = new DownloadTargetsResponse(List.of(
                new DownloadTargetResponse(3L, "최종 발표 자료", 2L, "AI/데이터", 6, 314572800L)));
        when(contestSubmissionFileQueryService.getDownloadTargets(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/submissions/downloads", 1)
                        .param("submissionTypeId", "3")
                        .param("trackId", "2")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-download-targets",
                        pathParameters(parameterWithName("contestId").description("대회 ID")),
                        queryParameters(
                                parameterWithName("submissionTypeId").description("제출물 종류 ID (선택)").optional(),
                                parameterWithName("trackId").description("분과 ID (선택)").optional()
                        ),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")),
                        responseFields(
                                arrayFieldWithPath("targets", "다운로드 대상 목록"),
                                numberFieldWithPath("targets[].submissionTypeId", "제출물 종류 ID"),
                                stringFieldWithPath("targets[].submissionTypeName", "제출물 종류명"),
                                numberFieldWithPath("targets[].trackId", "분과 ID"),
                                stringFieldWithPath("targets[].trackName", "분과명"),
                                numberFieldWithPath("targets[].submittedTeamCount", "제출 팀 수"),
                                numberFieldWithPath("targets[].estimatedSize", "예상 용량 (byte)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 선택한 대상의 제출 파일을 zip으로 다운로드한다.")
    void 선택한_대상의_제출_파일을_zip으로_다운로드한다() throws Exception {
        final StreamingResponseBody body = outputStream -> outputStream.write("zip-binary".getBytes());
        when(contestSubmissionFileQueryService.generateDownload(any(), any()))
                .thenReturn(new SubmissionDownload("2026-PNUops_20260605.zip", body));

        final SubmissionDownloadRequest request = new SubmissionDownloadRequest(List.of(
                new DownloadTargetRequest(3L, 2L), new DownloadTargetRequest(3L, 5L)));

        mockMvc.perform(post("/contests/{contestId}/submissions/downloads", 1)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("download-submissions",
                        pathParameters(parameterWithName("contestId").description("대회 ID")),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")),
                        requestFields(
                                arrayFieldWithPath("targets", "다운로드 대상 조합"),
                                numberFieldWithPath("targets[].submissionTypeId", "제출물 종류 ID"),
                                numberFieldWithPath("targets[].trackId", "분과 ID (선택, 생략 시 전체 분과)").optional()
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("attachment; filename=\"...zip\"")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 제출 파일을 개별 다운로드한다.")
    void 제출_파일을_개별_다운로드한다() throws Exception {
        when(contestSubmissionFileQueryService.downloadSubmissionFile(any(), any(), any()))
                .thenReturn(new DocumentFileDownload(new ByteArrayResource("pdf-binary".getBytes()),
                        "발표자료.pdf", "application/pdf", 1048576L, 12L));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/files/{fileId}", 1, 12, 101)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("download-submission-file",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출 ID"),
                                parameterWithName("fileId").description("파일 ID")
                        ),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)")),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("파일 MIME 타입"),
                                headerWithName(HttpHeaders.CONTENT_DISPOSITION).description("attachment; filename=\"발표자료.pdf\""),
                                headerWithName(HttpHeaders.CONTENT_LENGTH).description("파일 크기(byte)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 파일 개별 다운로드 시 404 에러를 반환한다.")
    void 존재하지_않는_파일_개별_다운로드_시_에러를_반환한다() throws Exception {
        willThrow(new FileException(NOT_FOUND))
                .given(contestSubmissionFileQueryService).downloadSubmissionFile(any(), any(), any());

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/files/{fileId}", 1, 12, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("download-submission-file-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출 ID"),
                                parameterWithName("fileId").description("존재하지 않는 파일 ID")
                        ),
                        requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (관리자)"))
                ));
    }
}
