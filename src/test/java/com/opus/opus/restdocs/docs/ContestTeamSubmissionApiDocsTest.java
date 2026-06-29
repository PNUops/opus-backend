package com.opus.opus.restdocs.docs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionSummaryResponse;
import com.opus.opus.modules.contest.domain.SubmissionStatus;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.restdocs.RestDocsTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.HttpHeaders;

public class ContestTeamSubmissionApiDocsTest extends RestDocsTest {

    private static final String MEMBER_TOKEN = "Bearer member.access.token";
    private static final String BASE_URL = "/contests/{contestId}/teams/{teamId}/submissions";

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember();
        setField(member, "id", 1L);

        when(memberArgumentResolver.supportsParameter(
                ArgumentMatchers.argThat(p -> p != null && p.hasParameterAnnotation(
                        com.opus.opus.global.security.annotation.LoginMember.class))))
                .thenReturn(true);
        when(memberArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(member);
    }

    @Test
    @DisplayName("[성공] 팀의 제출 현황 요약을 조회한다.")
    void 팀의_제출_현황_요약을_조회한다() throws Exception {
        final TeamSubmissionSummaryResponse response = new TeamSubmissionSummaryResponse(3L, 2L, 1L);

        when(contestSubmissionQueryService.getSubmissionSummary(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/summary", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-summary",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                numberFieldWithPath("totalItemCount", "전체 제출 항목 수"),
                                numberFieldWithPath("submittedCount", "제출 완료한 수"),
                                numberFieldWithPath("totalFeedbackCount", "전체 피드백 수")
                        )
                ));
    }

    @Test
    @DisplayName("[성공] 팀의 제출 타임라인을 조회한다.")
    void 팀의_제출_타임라인을_조회한다() throws Exception {
        final List<ContestSubmissionTimelineResponse> response = List.of(
                new ContestSubmissionTimelineResponse(SubmissionStatus.SUBMITTED, LocalDateTime.now().plusDays(7),
                        "기획서 제출"),
                new ContestSubmissionTimelineResponse(SubmissionStatus.LATE, LocalDateTime.now().minusDays(1),
                        "중간보고서 제출")
        );

        when(contestSubmissionQueryService.getSubmissionTimeline(any(), any(), any())).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/timeline", 1, 1)
                        .header(HttpHeaders.AUTHORIZATION, MEMBER_TOKEN))
                .andExpect(status().isOk())
                .andDo(document("get-submission-timeline",
                        pathParameters(
                                parameterWithName("contestId").description("대회 ID"),
                                parameterWithName("teamId").description("팀 ID")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        responseFields(
                                arrayFieldWithPath("[]", "제출 타임라인 목록"),
                                stringFieldWithPath("[].status", "제출 상태 (SUBMITTED: 정시 제출, LATE: 지각 제출)"),
                                dateTimeFieldWithPath("[].deadlineAt", "제출 마감 일시"),
                                stringFieldWithPath("[].submissionItemName", "제출 항목 이름")
                        )
                ));
    }
}
