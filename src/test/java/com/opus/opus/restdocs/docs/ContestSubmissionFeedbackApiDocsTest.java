package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.file.exception.FileExceptionType.EMPTY_FILE;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND_FEEDBACK_FILE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackFileResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackException;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class ContestSubmissionFeedbackApiDocsTest extends RestDocsTest {

    private static final String MENTOR_TOKEN = "Bearer mentor.access.token";
    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @BeforeEach
    void setUp() {
        final Member member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 외부멘토가 제출물 피드백을 저장하면 200을 반환한다.")
    void 외부멘토가_제출물_피드백을_저장하면_200을_반환한다() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "files", "피드백.pdf", MediaType.APPLICATION_PDF_VALUE, "feedback-content".getBytes());

        doNothing().when(contestSubmissionFeedbackCommandService)
                .saveFeedback(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 12)
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "발표 흐름이 좋네요. 데모 영상 길이만 조금 줄여보세요.")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(document("save-submission-feedback",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestParts(
                                partWithName("files").description("신규 추가 첨부파일 (선택, 다중 업로드 허용)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물에 피드백 저장 시 404 에러를 반환한다.")
    void 존재하지_않는_제출물에_피드백_저장_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionException(NOT_FOUND_SUBMISSION))
                .given(contestSubmissionFeedbackCommandService)
                .saveFeedback(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 999)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "발표 흐름이 좋네요.")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andDo(document("save-submission-feedback-fail-submission-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("존재하지 않는 제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 본문이 비어있으면 400 에러를 반환한다.")
    void 본문이_비어있으면_에러를_반환한다() throws Exception {
        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 12)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(document("save-submission-feedback-fail-blank",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에 피드백 저장 시 404 에러를 반환한다.")
    void 존재하지_않는_대회에_피드백_저장_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestSubmissionFeedbackCommandService)
                .saveFeedback(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 999, 12)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "발표 흐름이 좋네요.")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andDo(document("save-submission-feedback-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 외부멘토가 본인 피드백 단건을 조회할 수 있다.")
    void 외부멘토가_본인_피드백_단건을_조회할_수_있다() throws Exception {
        final ContestSubmissionMyFeedbackResponse response = new ContestSubmissionMyFeedbackResponse(
                50L, "발표 흐름이 좋네요. 데모 영상 길이만 조금 줄여보세요.",
                LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                List.of(new ContestSubmissionFeedbackFileResponse(201L, "피드백.pdf", 524288L))
        );

        when(contestSubmissionFeedbackQueryService.getFeedback(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/me", 1, 12)
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-my-submission-feedback",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                numberFieldWithPath("feedbackId", "피드백 ID"),
                                stringFieldWithPath("description", "피드백 본문"),
                                dateTimeFieldWithPath("createdAt", "작성 시각"),
                                dateTimeFieldWithPath("updatedAt", "마지막 수정 시각"),
                                arrayFieldWithPath("files", "첨부파일 목록"),
                                numberFieldWithPath("files[].fileId", "첨부파일 ID"),
                                stringFieldWithPath("files[].fileName", "첨부파일명"),
                                numberFieldWithPath("files[].fileSize", "첨부파일 용량 (byte)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 작성한 피드백이 없으면 단건 조회 시 404 에러를 반환한다.")
    void 작성한_피드백이_없으면_단건_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.getFeedback(any(), any(), any()))
                .thenThrow(new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/me", 1, 12)
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-my-submission-feedback-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 관리자가 제출물의 피드백 목록을 조회할 수 있다.")
    void 관리자가_제출물의_피드백_목록을_조회할_수_있다() throws Exception {
        final List<ContestSubmissionFeedbackResponse> response = List.of(
                new ContestSubmissionFeedbackResponse(
                        50L, 5L, "이지민", "발표 흐름이 좋네요. 데모 영상 길이만 조금 줄여보세요.",
                        LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                        LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                        List.of(new ContestSubmissionFeedbackFileResponse(201L, "피드백.pdf", 524288L))
                )
        );

        when(contestSubmissionFeedbackQueryService.getFeedbacks(any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 12)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-feedbacks",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "피드백 목록"),
                                numberFieldWithPath("[].feedbackId", "피드백 ID"),
                                numberFieldWithPath("[].memberId", "작성자 ID"),
                                stringFieldWithPath("[].memberName", "작성자 이름"),
                                stringFieldWithPath("[].description", "피드백 본문"),
                                dateTimeFieldWithPath("[].createdAt", "작성 시각"),
                                dateTimeFieldWithPath("[].updatedAt", "마지막 수정 시각"),
                                arrayFieldWithPath("[].files", "첨부파일 목록"),
                                numberFieldWithPath("[].files[].fileId", "첨부파일 ID"),
                                stringFieldWithPath("[].files[].fileName", "첨부파일명"),
                                numberFieldWithPath("[].files[].fileSize", "첨부파일 용량 (byte)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 피드백 목록 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_피드백_목록_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.getFeedbacks(any(), any()))
                .thenThrow(new ContestException(NOT_FOUND_CONTEST));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks", 999, 12)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-submission-feedbacks-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물의 피드백 목록 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_제출물의_피드백_목록_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.getFeedbacks(any(), any()))
                .thenThrow(new ContestSubmissionException(NOT_FOUND_SUBMISSION));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-submission-feedbacks-fail-submission-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("존재하지 않는 제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 관리자가 피드백 첨부파일을 다운로드할 수 있다.")
    void 관리자가_피드백_첨부파일을_다운로드할_수_있다() throws Exception {
        final FileDownload download = new FileDownload(
                new ByteArrayResource("feedback-file".getBytes()), "피드백.pdf", MediaType.APPLICATION_PDF_VALUE);

        when(contestSubmissionFeedbackQueryService.downloadFeedbackFile(any(), any(), any(), any()))
                .thenReturn(download);

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/{feedbackId}/files/{fileId}",
                        1, 12, 50, 201)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("download-submission-feedback-file",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("feedbackId").description("피드백 ID"),
                                parameterWithName("fileId").description("첨부파일 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 피드백의 파일 다운로드 시 404 에러를 반환한다.")
    void 존재하지_않는_피드백의_파일_다운로드_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.downloadFeedbackFile(any(), any(), any(), any()))
                .thenThrow(new ContestSubmissionFeedbackException(NOT_FOUND_FEEDBACK));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/{feedbackId}/files/{fileId}",
                        1, 12, 999, 201)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("download-submission-feedback-file-fail-feedback-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("feedbackId").description("존재하지 않는 피드백 ID"),
                                parameterWithName("fileId").description("첨부파일 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 피드백에 속하지 않은 파일 다운로드 시 404 에러를 반환한다.")
    void 피드백에_속하지_않은_파일_다운로드_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.downloadFeedbackFile(any(), any(), any(), any()))
                .thenThrow(new FileException(NOT_FOUND));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/{feedbackId}/files/{fileId}",
                        1, 12, 50, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("download-submission-feedback-file-fail-file-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("feedbackId").description("피드백 ID"),
                                parameterWithName("fileId").description("존재하지 않는 첨부파일 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 빈 파일을 첨부하면 400 에러를 반환한다.")
    void 빈_파일을_첨부하면_에러를_반환한다() throws Exception {
        final MockMultipartFile emptyFile = new MockMultipartFile(
                "files", "빈파일.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0]);

        willThrow(new FileException(EMPTY_FILE))
                .given(contestSubmissionFeedbackCommandService)
                .saveFeedback(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 12)
                        .file(emptyFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "발표 흐름이 좋네요.")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(document("save-submission-feedback-fail-empty-file",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 삭제할 첨부파일이 본인 피드백에 없으면 404 에러를 반환한다.")
    void 삭제할_첨부파일이_본인_피드백에_없으면_에러를_반환한다() throws Exception {
        willThrow(new FileException(NOT_FOUND_FEEDBACK_FILE))
                .given(contestSubmissionFeedbackCommandService)
                .saveFeedback(any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/feedbacks", 1, 12)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("description", "발표 흐름이 좋네요.")
                        .param("removeFileIds", "201")
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andDo(document("save-submission-feedback-fail-file-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 내 피드백 단건 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_내_피드백_단건_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.getFeedback(any(), any(), any()))
                .thenThrow(new ContestException(NOT_FOUND_CONTEST));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/me", 999, 12)
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-my-submission-feedback-fail-contest-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("존재하지 않는 대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물의 내 피드백 단건 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_제출물의_내_피드백_단건_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionFeedbackQueryService.getFeedback(any(), any(), any()))
                .thenThrow(new ContestSubmissionException(NOT_FOUND_SUBMISSION));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/feedbacks/me", 1, 999)
                        .header(HttpHeaders.AUTHORIZATION, MENTOR_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-my-submission-feedback-fail-submission-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("존재하지 않는 제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }
}
