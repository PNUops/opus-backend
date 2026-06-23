package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.COMMENT_NOT_BELONG_TO_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOTHING_TO_UPDATE;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
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
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionCommentFileResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionCommentResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionCommentException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class ContestSubmissionCommentApiDocsTest extends RestDocsTest {

    private static final String ADMIN_TOKEN = "Bearer admin.access.token";

    @BeforeEach
    void setUp() {
        final Member member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출물 코멘트가 정상적으로 등록된다.")
    void 유효한_요청이면_제출물_코멘트가_정상적으로_등록된다() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "files", "피드백.pdf", MediaType.APPLICATION_PDF_VALUE, "feedback-content".getBytes());

        doNothing().when(contestSubmissionCommentCommandService)
                .createComment(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments", 1, 12)
                        .file(file)
                        .param("description", "발표 흐름이 좋네요. 데모 영상 길이만 조금 줄여보세요.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andDo(document("create-submission-comment",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestParts(
                                partWithName("files").description("첨부파일 목록 (선택, 다중 업로드 허용)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물에 코멘트 등록 시 404 에러를 반환한다.")
    void 존재하지_않는_제출물에_코멘트_등록_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionException(NOT_FOUND_SUBMISSION))
                .given(contestSubmissionCommentCommandService)
                .createComment(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments", 1, 999)
                        .param("description", "발표 흐름이 좋네요.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andDo(document("create-submission-comment-fail-not-found",
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
        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments", 1, 12)
                        .param("description", "")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andDo(document("create-submission-comment-fail-blank",
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
    @DisplayName("[성공] 제출물의 코멘트 목록을 정상적으로 조회할 수 있다.")
    void 제출물의_코멘트_목록을_정상적으로_조회할_수_있다() throws Exception {
        final List<ContestSubmissionCommentResponse> response = List.of(
                new ContestSubmissionCommentResponse(
                        31L, 5L, "이지민", "발표 흐름이 좋네요. 데모 영상 길이만 조금 줄여보세요.",
                        LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                        LocalDateTime.of(2026, 6, 2, 11, 0, 0),
                        "ROLE_교수",
                        List.of(new ContestSubmissionCommentFileResponse(201L, "피드백.pdf", 524288L))
                )
        );

        when(contestSubmissionCommentQueryService.getComments(any(), any())).thenReturn(response);

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/comments", 1, 12)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-comments",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "코멘트 목록"),
                                numberFieldWithPath("[].commentId", "코멘트 ID"),
                                numberFieldWithPath("[].memberId", "작성자 ID"),
                                stringFieldWithPath("[].memberName", "작성자 이름"),
                                stringFieldWithPath("[].description", "코멘트 본문"),
                                dateTimeFieldWithPath("[].createdAt", "작성 시각"),
                                dateTimeFieldWithPath("[].updatedAt", "마지막 수정 시각"),
                                stringFieldWithPath("[].roleType", "작성자 역할 (MemberRoleType)"),
                                arrayFieldWithPath("[].files", "첨부파일 목록"),
                                numberFieldWithPath("[].files[].fileId", "첨부파일 ID"),
                                stringFieldWithPath("[].files[].fileName", "첨부파일명"),
                                numberFieldWithPath("[].files[].fileSize", "첨부파일 용량 (byte)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 유효한 요청이면 제출물 코멘트가 정상적으로 수정된다.")
    void 유효한_요청이면_제출물_코멘트가_정상적으로_수정된다() throws Exception {
        final MockMultipartFile addFile = new MockMultipartFile(
                "addFiles", "추가피드백.pdf", MediaType.APPLICATION_PDF_VALUE, "more-feedback".getBytes());

        doNothing().when(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .file(addFile)
                        .param("description", "코멘트 본문을 수정합니다.")
                        .param("removeFileIds", "201")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andDo(document("update-submission-comment",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("수정할 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        requestParts(
                                partWithName("addFiles").description("새로 추가할 첨부파일 (선택, 다중 업로드 허용)").optional()
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 코멘트 수정 시 403 에러를 반환한다.")
    void 본인이_작성하지_않은_코멘트_수정_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(NOT_OWNER_COMMENT))
                .given(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .param("description", "코멘트 본문을 수정합니다.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isForbidden())
                .andDo(document("update-submission-comment-fail-not-owner",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("다른 사람이 작성한 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 제출물 코멘트가 정상적으로 삭제된다.")
    void 제출물_코멘트가_정상적으로_삭제된다() throws Exception {
        doNothing().when(contestSubmissionCommentCommandService).deleteComment(any(), any(), any(), any());

        mockMvc.perform(delete("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-submission-comment",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("삭제할 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 코멘트 삭제 시 403 에러를 반환한다.")
    void 본인이_작성하지_않은_코멘트_삭제_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(NOT_OWNER_COMMENT))
                .given(contestSubmissionCommentCommandService)
                .deleteComment(any(), any(), any(), any());

        mockMvc.perform(delete("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isForbidden())
                .andDo(document("delete-submission-comment-fail-not-owner",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("다른 사람이 작성한 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에 코멘트 등록 시 404 에러를 반환한다.")
    void 존재하지_않는_대회에_코멘트_등록_시_에러를_반환한다() throws Exception {
        willThrow(new ContestException(NOT_FOUND_CONTEST))
                .given(contestSubmissionCommentCommandService)
                .createComment(any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments", 999, 12)
                        .param("description", "발표 흐름이 좋네요.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andDo(document("create-submission-comment-fail-contest-not-found",
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
    @DisplayName("[실패] 존재하지 않는 대회의 코멘트 목록 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_대회의_코멘트_목록_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionCommentQueryService.getComments(any(), any()))
                .thenThrow(new ContestException(NOT_FOUND_CONTEST));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/comments", 999, 12)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-submission-comments-fail-contest-not-found",
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
    @DisplayName("[실패] 존재하지 않는 제출물의 코멘트 목록 조회 시 404 에러를 반환한다.")
    void 존재하지_않는_제출물의_코멘트_목록_조회_시_에러를_반환한다() throws Exception {
        when(contestSubmissionCommentQueryService.getComments(any(), any()))
                .thenThrow(new ContestSubmissionException(NOT_FOUND_SUBMISSION));

        mockMvc.perform(get("/contests/{contestId}/submissions/{submissionId}/comments", 1, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-submission-comments-fail-submission-not-found",
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
    @DisplayName("[실패] 수정할 내용이 모두 비어있으면 400 에러를 반환한다.")
    void 수정할_내용이_모두_비어있으면_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(NOTHING_TO_UPDATE))
                .given(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andDo(document("update-submission-comment-fail-nothing-to-update",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("수정할 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 코멘트 수정 시 404 에러를 반환한다.")
    void 존재하지_않는_코멘트_수정_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(NOT_FOUND_COMMENT))
                .given(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 999)
                        .param("description", "코멘트 본문을 수정합니다.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andDo(document("update-submission-comment-fail-comment-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("존재하지 않는 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 코멘트가 해당 제출물에 속하지 않으면 400 에러를 반환한다.")
    void 코멘트가_해당_제출물에_속하지_않으면_수정_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(COMMENT_NOT_BELONG_TO_SUBMISSION))
                .given(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .param("description", "코멘트 본문을 수정합니다.")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andDo(document("update-submission-comment-fail-not-belong",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("다른 제출물에 속한 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 삭제할 첨부파일을 찾을 수 없으면 404 에러를 반환한다.")
    void 삭제할_첨부파일을_찾을_수_없으면_에러를_반환한다() throws Exception {
        willThrow(new FileException(NOT_FOUND, "삭제할 파일을 찾을 수 없습니다. ID=201"))
                .given(contestSubmissionCommentCommandService)
                .updateComment(any(), any(), any(), any(), any(), any(), any());

        mockMvc.perform(multipart("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .param("removeFileIds", "201")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isNotFound())
                .andDo(document("update-submission-comment-fail-file-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 코멘트 삭제 시 404 에러를 반환한다.")
    void 존재하지_않는_코멘트_삭제_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(NOT_FOUND_COMMENT))
                .given(contestSubmissionCommentCommandService)
                .deleteComment(any(), any(), any(), any());

        mockMvc.perform(delete("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 999)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("delete-submission-comment-fail-comment-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("존재하지 않는 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 코멘트가 해당 제출물에 속하지 않으면 삭제 시 400 에러를 반환한다.")
    void 코멘트가_해당_제출물에_속하지_않으면_삭제_시_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionCommentException(COMMENT_NOT_BELONG_TO_SUBMISSION))
                .given(contestSubmissionCommentCommandService)
                .deleteComment(any(), any(), any(), any());

        mockMvc.perform(delete("/contests/{contestId}/submissions/{submissionId}/comments/{commentId}", 1, 12, 31)
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN))
                .andExpect(status().isBadRequest())
                .andDo(document("delete-submission-comment-fail-not-belong",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("submissionId").description("제출물 ID"),
                                parameterWithName("commentId").description("다른 제출물에 속한 코멘트 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        )
                ));
    }
}
