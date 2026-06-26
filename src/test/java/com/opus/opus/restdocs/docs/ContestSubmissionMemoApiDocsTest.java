package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
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
import com.opus.opus.modules.member.domain.Member;
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
    private static final String BASE_URL = "/contests/{contestId}/teams/{teamId}/submissions/{submissionId}/memos";

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
                                parameterWithName("submissionId").description("제출물 ID")
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
                                parameterWithName("submissionId").description("제출물 ID")
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
                                parameterWithName("submissionId").description("제출물 ID")
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
                                parameterWithName("submissionId").description("제출물 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken} (팀원)")
                        )
                ));
    }
}
