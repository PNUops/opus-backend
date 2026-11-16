package com.opus.opus.restdocs.docs;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_ITEM_FOR_TRACK;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.MEMO_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
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
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionMemoRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMemoResponse;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ContestSubmissionMemoApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";
    private static final String BASE_URL = "/contests/{contestId}/teams/{teamId}/submission-items/{submissionItemId}/memos";

    private Member member;
    private ContestSubmissionMemoRequest request;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(
                ArgumentMatchers.argThat(p -> p != null && p.hasParameterAnnotation(
                        com.opus.opus.global.security.annotation.LoginMember.class))))
                .thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);

        request = new ContestSubmissionMemoRequest("제출물에 대한 메모 내용입니다.");
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 메모를 생성한다.")
    void 팀원이_제출물_메모를_생성한다() throws Exception {
        doNothing().when(contestSubmissionMemoCommandService).createMemo(any(), any(), any(), any(), any());

        mockMvc.perform(post(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("create-submission-memo",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        ),
                        requestFields(
                                stringFieldWithPath("content", "메모 내용 (최대 500자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 메모를 조회한다.")
    void 팀원이_제출물_메모를_조회한다() throws Exception {
        final ContestSubmissionMemoResponse response =
                new ContestSubmissionMemoResponse(1L, "제출물에 대한 메모 내용입니다.", LocalDateTime.now());

        when(contestSubmissionMemoQueryService.getMemo(any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-memo",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        ),
                        responseFields(
                                numberFieldWithPath("memoId", "메모 ID"),
                                stringFieldWithPath("content", "메모 내용"),
                                dateTimeFieldWithPath("updatedAt", "수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 메모를 수정한다.")
    void 팀원이_제출물_메모를_수정한다() throws Exception {
        doNothing().when(contestSubmissionMemoCommandService).updateMemo(any(), any(), any(), any(), any());

        mockMvc.perform(patch(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andDo(document("update-submission-memo",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        ),
                        requestFields(
                                stringFieldWithPath("content", "수정할 메모 내용 (최대 500자)")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 메모를 삭제한다.")
    void 팀원이_제출물_메모를_삭제한다() throws Exception {
        doNothing().when(contestSubmissionMemoCommandService).deleteMemo(any(), any(), any(), any());

        mockMvc.perform(delete(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNoContent())
                .andDo(document("delete-submission-memo",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않은 팀의 메모를 생성하면 400 에러를 반환한다.")
    void 대회에_속하지_않은_팀의_메모를_생성하면_400_에러를_반환한다() throws Exception {
        willThrow(new TeamException(TEAM_NOT_IN_CONTEST))
                .given(contestSubmissionMemoCommandService)
                .createMemo(any(), any(), any(), any(), any());

        mockMvc.perform(post(BASE_URL, 1, 99, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-submission-memo-fail-team-not-in-contest",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("대회에 속하지 않은 팀 ID"),
                                parameterWithName("submissionItemId").description("제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않은 제출 항목에 메모를 생성하면 400 에러를 반환한다.")
    void 대회에_속하지_않은_제출_항목에_메모를_생성하면_400_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionItemException(INVALID_SUBMISSION_ITEM_FOR_CONTEST))
                .given(contestSubmissionMemoCommandService)
                .createMemo(any(), any(), any(), any(), any());

        mockMvc.perform(post(BASE_URL, 1, 1, 99)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-submission-memo-fail-submission-item-not-in-contest",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("대회에 속하지 않은 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 팀의 분과가 아닌 제출 항목에 메모를 생성하면 400 에러를 반환한다.")
    void 팀의_분과가_아닌_제출_항목에_메모를_생성하면_400_에러를_반환한다() throws Exception {
        willThrow(new ContestException(INVALID_SUBMISSION_ITEM_FOR_TRACK))
                .given(contestSubmissionMemoCommandService)
                .createMemo(any(), any(), any(), any(), any());

        mockMvc.perform(post(BASE_URL, 1, 1, 2)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("create-submission-memo-fail-invalid-track",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("팀의 분과가 아닌 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 이미 메모가 있는 제출 항목에 메모를 생성하면 409 에러를 반환한다.")
    void 이미_메모가_있는_제출_항목에_메모를_생성하면_409_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionMemoException(MEMO_ALREADY_EXISTS))
                .given(contestSubmissionMemoCommandService)
                .createMemo(any(), any(), any(), any(), any());

        mockMvc.perform(post(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(document("create-submission-memo-fail-already-exists",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("이미 메모가 있는 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 메모가 없는 제출 항목의 메모를 조회하면 404 에러를 반환한다.")
    void 메모가_없는_제출_항목의_메모를_조회하면_404_에러를_반환한다() throws Exception {
        when(contestSubmissionMemoQueryService.getMemo(any(), any(), any(), any()))
                .thenThrow(new ContestSubmissionMemoException(NOT_FOUND_MEMO));

        mockMvc.perform(get(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("get-submission-memo-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("메모가 없는 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 메모가 없는 제출 항목의 메모를 수정하면 404 에러를 반환한다.")
    void 메모가_없는_제출_항목의_메모를_수정하면_404_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionMemoException(NOT_FOUND_MEMO))
                .given(contestSubmissionMemoCommandService)
                .updateMemo(any(), any(), any(), any(), any());

        mockMvc.perform(patch(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(document("update-submission-memo-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("메모가 없는 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }

    @Test
    @DisplayName("[실패] 메모가 없는 제출 항목의 메모를 삭제하면 404 에러를 반환한다.")
    void 메모가_없는_제출_항목의_메모를_삭제하면_404_에러를_반환한다() throws Exception {
        willThrow(new ContestSubmissionMemoException(NOT_FOUND_MEMO))
                .given(contestSubmissionMemoCommandService)
                .deleteMemo(any(), any(), any(), any());

        mockMvc.perform(delete(BASE_URL, 1, 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isNotFound())
                .andDo(document("delete-submission-memo-fail-not-found",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID"),
                                parameterWithName("submissionItemId").description("메모가 없는 제출 항목 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }
}
